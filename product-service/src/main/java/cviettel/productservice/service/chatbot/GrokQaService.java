package cviettel.productservice.service.chatbot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class GrokQaService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // URL cơ bản của API x.ai, ví dụ: https://api.x.ai/v1
    @Value("${xai.api.url}")
    private String xaiApiUrl;

    @Value("${xai.api.key}")
    private String xaiApiKey;

    public GrokQaService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * Phân tích câu hỏi và danh sách các field liên quan đến sản phẩm.
     * Yêu cầu API x.ai trả về một JSON object chứa các tiêu chí truy vấn.
     *
     * Ví dụ prompt gửi đi:
     * "Phân tích câu hỏi: 'Tôi cần mua một laptop có cấu hình mạnh, giá hợp lý'
     * và danh sách field: [productName, productPrice, productDetails]. Trả về JSON tiêu chí,
     * ví dụ: {\"productName\": \"Laptop\", \"productPrice\": \"1500\", \"productDetails\": \"mạnh mẽ\"}"
     */
    public Map<String, Object> analyzeQuery(String question, List<String> productFields) {
        String prompt = "Phân tích câu hỏi: \"" + question + "\"\n"
                + "Danh sách field liên quan: " + productFields.toString() + "\n"
                + "Trả về một JSON object chứa các tiêu chí truy vấn phù hợp. Ví dụ: {\"productName\": \"Laptop\", \"productPrice\": \"1500\"}";

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "grok-2-latest");
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", prompt));
        payload.put("messages", messages);
        payload.put("temperature", 0.0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + xaiApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        log.info("url to grok chatbot: {}", xaiApiUrl + "/chat/completions");
        ResponseEntity<Map> response = restTemplate.postForEntity(xaiApiUrl + "/chat/completions", request, Map.class);
        log.info("Response from grok chatbot: {}", response);
        log.info("Response body from grok chatbot: {}", response.getBody());

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            try {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    String content = (String) message.get("content");
                    // Trích xuất phần JSON từ content (nếu có)
                    String jsonPart = extractJson(content);
                    if (jsonPart != null) {
                        return objectMapper.readValue(jsonPart, new TypeReference<Map<String, Object>>() {});
                    } else {
                        log.warn("Không tìm thấy phần JSON hợp lệ trong nội dung: {}", content);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new HashMap<>();
    }

    /**
     * Tổng hợp câu trả lời tư vấn dựa trên câu hỏi của khách hàng và dữ liệu sản phẩm.
     * Yêu cầu API x.ai tạo ra câu trả lời cuối cùng.
     */
    public String assembleAnswer(String question, List<Map<String, Object>> productData) {
        String prompt = "Dựa vào dữ liệu sản phẩm dưới đây và câu hỏi của khách hàng, hãy tư vấn sản phẩm phù hợp.\n"
                + "Câu hỏi: " + question + "\n"
                + "Dữ liệu sản phẩm: " + productData.toString() + "\n"
                + "Hãy đưa ra câu trả lời chi tiết và gợi ý các lựa chọn tốt nhất.";

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "grok-2-latest");
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", prompt));
        payload.put("messages", messages);
        payload.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + xaiApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(xaiApiUrl + "/chat/completions", request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            try {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    return (String) message.get("content");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "Xin lỗi, hệ thống không thể tư vấn lúc này.";
    }

    /**
     * Trích xuất chuỗi JSON từ nội dung văn bản nếu có chứa khối JSON.
     * Nếu tìm thấy khối được định dạng bằng ```json ... ```, thì trả về nội dung giữa chúng.
     * Nếu không, cố gắng lấy chuỗi từ dấu '{' đầu tiên đến '}' cuối cùng.
     */
    private String extractJson(String content) {
        // Tìm khối JSON được bao quanh bởi ```json và ```
        Pattern pattern = Pattern.compile("```json\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        // Nếu không tìm thấy, cố gắng lấy chuỗi từ '{' đến '}'.
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return content.substring(start, end + 1).trim();
        }
        return null;
    }
}
