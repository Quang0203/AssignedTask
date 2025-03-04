package cviettel.apigateway.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class JwtService {
    // Trích từ key thứ hai (alg=RS256, use=sig) trong JSON JWKS của bạn
    // Lưu ý: decode base64Url, nên dùng Base64.getUrlDecoder()
    // N của realm SpringAPI
//    private static final String N = "pnkU28JWUC008teSc7IRRFqpO7dcbm8Wze15kw68KMCEXAd7NxkSVQhcrUNonDliigqn58369J5ZYr4eXiIVX71uCO-hp3F-FHxE3Ak9QRfyvpqB3DedsBK2dmnN_BiAg3ZdTV1UEVOXaxDTgo6ACkGFo1zK_IG33_X6eaOjwLGQck7BtB8dm6XloHbxxiL4YflCfH9-xoiYvx3H8UtZ9vM-kJZCs5mufZhREfLdgrSeCPGU65oIpDTahvWeA6fGpkXOlrzmjMRW33Y0Ve3edWAqrACBhpsG1vxF7Z4dH6biif8ii9c48-QbtrJ__CcyD0imrvmrTTJXAyFNC8qFbw";
    // N của realm test để ký
    private static final String N = "s2YYABxxWbqHLO1fkdRC_EkVIvApvEMTAgs-zT5tmGCkXkSRlOygTp7kSb0eYynl0BRthBPrQvqofbZoVaOR4uRWd81raIXbbnOFYKjvriq-j1aFeqZz89XRmV-iW-y1NYx2GIi6FSDOP0gdTPr-2DJ1mTfCgLfNgLpij2a-A1pzCFOVsDIrm0wuGzCW5xBGNAw4BNmUqOoNpM3Ad-9YqyPKeNvDYoIzHBBrqFPzW2M4LInP-4v_Pkh82hzP4kSPFBq8F_MaaaIhYDelMbe8d2TTI7FG9UkMQ7YMzEP1nxJb8VYVOXGj4btM4-SAhTSYPORrgza48PPmwJAaDB3Mxw";
    private static final String E = "AQAB";

    private final RedisTemplate<String, Object> redisTemplate;

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

    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email", String.class);
    }

    public boolean validateToken(String token) {
        String tokenCache = redisTemplate.opsForValue().get("TokenLogin") + "";
        if (tokenCache.isEmpty()) {
            return false;
        }
        return (token.equals(tokenCache));
    }
}

