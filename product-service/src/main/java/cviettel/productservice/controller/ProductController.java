package cviettel.productservice.controller;

import cviettel.productservice.dto.request.ProductRequest;
import cviettel.productservice.entity.Product;
import cviettel.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // Lấy danh sách tất cả sản phẩm
    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    // Lấy chi tiết sản phẩm theo id
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") String id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    // Tạo mới sản phẩm
    @PostMapping("/new-product")
    public ResponseEntity<String> createProduct(@RequestBody ProductRequest product) {
        String newProduct = productService.createProduct(product);
        return new ResponseEntity<>("Successful creating", HttpStatus.CREATED);
    }

    // Cập nhật sản phẩm theo id
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable("id") String id,
                                                 @RequestBody Product product) {
        Product updatedProduct = productService.updateProduct(id, product);
        return ResponseEntity.ok(updatedProduct);
    }

    // Xoá sản phẩm theo id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}