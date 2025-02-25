//package cviettel.loginservice.util;
//
//import cviettel.loginservice.util.RSA;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.core.MethodParameter;
//import org.springframework.http.MediaType;
//import org.springframework.http.converter.HttpMessageConverter;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
//
//@ControllerAdvice
//public class RSAEncryptionResponseBodyAdvice implements ResponseBodyAdvice<Object> {
//
//    private RSA rsa;
//
//    public RSAEncryptionResponseBodyAdvice() {
//        rsa = new RSA();
//        rsa.initFromStrings();
//    }
//
//    @Override
//    public boolean supports(MethodParameter returnType,
//                            Class<? extends HttpMessageConverter<?>> converterType) {
//        // Bạn có thể tùy chỉnh điều kiện áp dụng (ví dụ: chỉ áp dụng cho một số API nhất định)
//        return true;
//    }
//
//    @Override
//    public Object beforeBodyWrite(Object body,
//                                  MethodParameter returnType,
//                                  MediaType selectedContentType,
//                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
//                                  ServerHttpRequest request,
//                                  ServerHttpResponse response) {
//        try {
//            // Chuyển đổi object thành JSON
//            String json = new ObjectMapper().writeValueAsString(body);
//            // Mã hoá chuỗi JSON bằng RSA
//            return rsa.encrypt(json);
//        } catch (Exception e) {
//            throw new RuntimeException("Mã hoá RSA phản hồi thất bại", e);
//        }
//    }
//}
