package cviettel.productservice.service.chatbot;

import cviettel.productservice.entity.Product;
import cviettel.productservice.repository.ProductRepository;
import cviettel.productservice.util.ChatSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatbotService {

    private final GrokQaService grokQaService;
    private final ProductRepository productRepository;

    public ChatbotService(GrokQaService grokQaService, ProductRepository productRepository) {
        this.grokQaService = grokQaService;
        this.productRepository = productRepository;
    }

    public String handleChat(String question, ChatSession chatSession) {
        // Thêm câu hỏi của user vào phiên chat
        chatSession.addMessage("user", question);

        // Lấy dữ liệu sản phẩm từ DB (ở đây đơn giản lấy tất cả sản phẩm)
        List<Product> products = productRepository.findAll();

        // Chuyển dữ liệu sản phẩm thành danh sách Map
        List<Map<String, Object>> productData = products.stream().map(product -> {
            Map<String, Object> map = new HashMap<>();
            map.put("productName", product.getProductName());
            map.put("productPrice", product.getProductPrice());
            map.put("productDetails", product.getProductDetails());
            return map;
        }).collect(Collectors.toList());

        // Tạo một message hệ thống chứa thông tin sản phẩm (có thể tùy chỉnh theo yêu cầu)
        String productsInfo = "Dữ liệu sản phẩm: " + productData.toString();
        chatSession.addMessage("system", productsInfo);

        // Gọi API x.ai, gửi toàn bộ lịch sử hội thoại để mô hình có thể hiểu ngữ cảnh
        String answer = grokQaService.assembleAnswerWithHistory(chatSession.getMessages());

        log.info("Assistant answer: {}", answer);

        // Thêm câu trả lời của assistant vào phiên chat
        chatSession.addMessage("assistant", answer);

        log.info("ChatSession: {}", chatSession.getMessages());

        return answer;
    }
}
