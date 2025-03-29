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

@Slf4j
@Service
public class GrokQaService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${xai.api.url}")
    private String xaiApiUrl;

    @Value("${xai.api.key}")
    private String xaiApiKey;

    public GrokQaService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * Tổng hợp câu trả lời dựa trên toàn bộ lịch sử hội thoại (messages).
     * Phương thức này gửi payload chứa danh sách các message (user, assistant, system)
     * cho API x.ai để giữ được ngữ cảnh của cuộc trò chuyện.
     */
    public String assembleAnswerWithHistory(List<Map<String, String>> messages) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "grok-2-latest");
        payload.put("messages", messages);
        payload.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + xaiApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        log.info("Request to x.ai: {}", request);
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
}
