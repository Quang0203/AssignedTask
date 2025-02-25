package cviettel.loginservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {

//    // Replace this with a secure key in a real application, ideally fetched from environment variables
//    @Value("${security.authentication.jwt.base64-secret}")
//    public String SECRET;
//
//    // Generate token with given user name
//    public String generateToken(String email) {
//        Map<String, Object> claims = new HashMap<>();
//        return createToken(claims, email);
//    }
//
//    // Create a JWT token with specified claims and subject (user name)
//    private String createToken(Map<String, Object> claims, String userName) {
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(userName)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 1)) // Token valid for 30 minutes
//                .signWith(getSignKey(), SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    // Get the signing key for JWT token
//    private Key getSignKey() {
//        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }
//
//    // Extract the username from the token
//    public String extractUsername(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//
//    // Extract the expiration date from the token
//    public Date extractExpiration(String token) {
//        return extractClaim(token, Claims::getExpiration);
//    }
//
//    // Extract a claim from the token
//    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = extractAllClaims(token);
//        return claimsResolver.apply(claims);
//    }
//
//    // Extract all claims from the token
//    private Claims extractAllClaims(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(getSignKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    // Check if the token is expired
//    private Boolean isTokenExpired(String token) {
//        return extractExpiration(token).before(new Date());
//    }
//
//    // Validate the token against user details and expiration
//    public Boolean validateToken(String token, UserDetails userDetails){
//        final String username = extractUsername(token);
//        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
//    }

    // Trích từ key thứ hai (alg=RS256, use=sig) trong JSON JWKS của bạn
    // Lưu ý: decode base64Url, nên dùng Base64.getUrlDecoder()
    private static final String N = "pnkU28JWUC008teSc7IRRFqpO7dcbm8Wze15kw68KMCEXAd7NxkSVQhcrUNonDliigqn58369J5ZYr4eXiIVX71uCO-hp3F-FHxE3Ak9QRfyvpqB3DedsBK2dmnN_BiAg3ZdTV1UEVOXaxDTgo6ACkGFo1zK_IG33_X6eaOjwLGQck7BtB8dm6XloHbxxiL4YflCfH9-xoiYvx3H8UtZ9vM-kJZCs5mufZhREfLdgrSeCPGU65oIpDTahvWeA6fGpkXOlrzmjMRW33Y0Ve3edWAqrACBhpsG1vxF7Z4dH6biif8ii9c48-QbtrJ__CcyD0imrvmrTTJXAyFNC8qFbw";
    private static final String E = "AQAB";

    private PublicKey getPublicKey() {
        try {
            // Base64Url decode n, e
            byte[] nBytes = Base64.getUrlDecoder().decode(N);
            byte[] eBytes = Base64.getUrlDecoder().decode(E);

            // Chuyển thành BigInteger
            BigInteger modulus = new BigInteger(1, nBytes);
            BigInteger exponent = new BigInteger(1, eBytes);

            // Tạo PublicKey (RSA)
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
            return keyFactory.generatePublic(keySpec);

        } catch (Exception ex) {
            throw new RuntimeException("Lỗi khi tạo Public Key từ n,e", ex);
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getPublicKey()) // Dùng Public Key
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email", String.class);
    }

    public boolean validateToken(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
//        final String username = extractUsername(token);
        final String username = extractEmail(token);
        return (username.equals(userDetails.getUsername()));
    }
}

