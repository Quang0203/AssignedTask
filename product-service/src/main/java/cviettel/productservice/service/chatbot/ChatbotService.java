package cviettel.productservice.service.chatbot;

import cviettel.productservice.repository.ProductRepository;
import cviettel.productservice.entity.Product;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private final GrokQaService grokQaService;
    private final ProductRepository productRepository;

    public ChatbotService(GrokQaService grokQaService, ProductRepository productRepository) {
        this.grokQaService = grokQaService;
        this.productRepository = productRepository;
    }

    public String handleChat(String question) {
        // Bước 1: Gửi câu hỏi và danh sách field cho API để phân tích tiêu chí truy vấn liên quan đến sản phẩm.
        List<String> productFields = Arrays.asList("productName", "productPrice", "productDetails");
        // API x.ai sẽ trả về ví dụ: {"productName": "Laptop", "productPrice": "1500", "productDetails": "Mạnh mẽ, nhẹ"}
        Map<String, Object> criteria = grokQaService.analyzeQuery(question, productFields);

        // Nếu bạn muốn áp dụng tiêu chí từ criteria để lọc sản phẩm, có thể thực hiện tại đây.
        // Ví dụ: Lọc theo productName chứa một từ khóa nào đó.
        // Ở đây, ví dụ đơn giản: lấy tất cả sản phẩm
        List<Product> products = productRepository.findAll();

        // Chuyển đổi danh sách sản phẩm thành danh sách Map để gửi cho API x.ai
        List<Map<String, Object>> productData = products.stream().map(product -> {
            Map<String, Object> map = new HashMap<>();
            map.put("productName", product.getProductName());
            map.put("productPrice", product.getProductPrice());
            map.put("productDetails", product.getProductDetails());
            return map;
        }).collect(Collectors.toList());

        // Bước 2: Gửi câu hỏi và dữ liệu sản phẩm cho API x.ai để tổng hợp câu trả lời cuối cùng
        return grokQaService.assembleAnswer(question, productData);
    }
}
