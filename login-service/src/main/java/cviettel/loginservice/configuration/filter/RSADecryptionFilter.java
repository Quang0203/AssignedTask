//package cviettel.loginservice.configuration.filter;
//
//import cviettel.loginservice.util.RSA;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletRequestWrapper;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
//
//@Component
//public class RSADecryptionFilter extends OncePerRequestFilter {
//
//    private RSA rsa;
//
//    public RSADecryptionFilter() {
//        rsa = new RSA();
//        rsa.initFromStrings();
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain)
//            throws ServletException, IOException {
//        System.out.println("RSADecryptionFilter.doFilterInternal " + request.getRequestURI());
//
//        // Áp dụng cho endpoint /login với phương thức POST
//        if ("/auth/login".equals(request.getRequestURI())
//                && "POST".equalsIgnoreCase(request.getMethod())) {
//            System.out.println("RSADecryptionFilter.doFilterInternal: /auth/login POST");
//            // Đọc toàn bộ body (dữ liệu đã mã hoá RSA)
//            String encryptedBody = readBody(request.getInputStream());
//            System.out.println("RSADecryptionFilter.doFilterInternal: encryptedBody = " + encryptedBody);
//            String decryptedBody;
//            try {
//                decryptedBody = rsa.decrypt(encryptedBody);
//                System.out.println("RSADecryptionFilter.doFilterInternal: decryptedBody = " + decryptedBody);
//            } catch (Exception e) {
//                throw new ServletException("Giải mã RSA thất bại", e);
//            }
//            // Gói request với body đã giải mã
//            HttpServletRequest wrappedRequest = new CustomHttpServletRequestWrapper(request, decryptedBody);
//            System.out.println("RSADecryptionFilter.doFilterInternal: wrappedRequest = " + wrappedRequest);
//            filterChain.doFilter(wrappedRequest, response);
//        } else {
//            filterChain.doFilter(request, response);
//        }
//    }
//
//    private String readBody(InputStream inputStream) throws IOException {
//        StringBuilder stringBuilder = new StringBuilder();
//        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                stringBuilder.append(line);
//            }
//        }
//        return stringBuilder.toString();
//    }
//}
