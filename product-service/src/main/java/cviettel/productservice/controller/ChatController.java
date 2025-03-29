package cviettel.productservice.controller;

import cviettel.productservice.service.chatbot.ChatbotService;
import cviettel.productservice.util.ChatSession;
import cviettel.productservice.util.ChatSessionManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatbotService chatbotService;

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String question = payload.get("question");

        // Lấy sessionId từ header "X-Session-Id", nếu không có thì tạo mới
        String sessionId = request.getHeader("X-Session-Id");
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        // Lấy ChatSession từ ChatSessionManager
        ChatSession chatSession = ChatSessionManager.getSession(sessionId);
        if (chatSession == null) {
            chatSession = new ChatSession();
            ChatSessionManager.saveSession(sessionId, chatSession);
            System.out.println("Created new ChatSession for sessionId: " + sessionId);
        } else {
            System.out.println("Using existing ChatSession for sessionId: " + sessionId);
        }

        String answer = chatbotService.handleChat(question, chatSession);
        System.out.println("Current ChatSession messages: " + chatSession.getMessages());

        // Trả về sessionId cho FE để lưu và gửi kèm theo các request sau
        return ResponseEntity.ok(Map.of("answer", answer, "sessionId", sessionId));
    }
}
