package cviettel.productservice.controller;

import cviettel.productservice.service.chatbot.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatbotService chatbotService;

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");
        String answer = chatbotService.handleChat(question);
        return ResponseEntity.ok(Collections.singletonMap("answer", answer));
    }
}
