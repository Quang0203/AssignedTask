package cviettel.orderservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class JwtService {

    // Trích từ key thứ hai (alg=RS256, use=sig) trong JSON JWKS của bạn
    // Lưu ý: decode base64Url, nên dùng Base64.getUrlDecoder()
    private static final String N = "s2YYABxxWbqHLO1fkdRC_EkVIvApvEMTAgs-zT5tmGCkXkSRlOygTp7kSb0eYynl0BRthBPrQvqofbZoVaOR4uRWd81raIXbbnOFYKjvriq-j1aFeqZz89XRmV-iW-y1NYx2GIi6FSDOP0gdTPr-2DJ1mTfCgLfNgLpij2a-A1pzCFOVsDIrm0wuGzCW5xBGNAw4BNmUqOoNpM3Ad-9YqyPKeNvDYoIzHBBrqFPzW2M4LInP-4v_Pkh82hzP4kSPFBq8F_MaaaIhYDelMbe8d2TTI7FG9UkMQ7YMzEP1nxJb8VYVOXGj4btM4-SAhTSYPORrgza48PPmwJAaDB3Mxw";
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

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Map<String, Object> realmAccess = claims.get("realm_access", Map.class);
        if (realmAccess != null && realmAccess.get("roles") != null) {
            return (List<String>) realmAccess.get("roles");
        }
        return List.of();
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

