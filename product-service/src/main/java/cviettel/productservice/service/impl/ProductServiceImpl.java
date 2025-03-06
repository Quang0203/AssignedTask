package cviettel.productservice.service.impl;

import cviettel.productservice.dto.request.ProductRequest;
import cviettel.productservice.entity.Product;
import cviettel.productservice.repository.ProductRepository;
import cviettel.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    // Lấy danh sách sản phẩm và cache kết quả.
    @Override
    @Cacheable(value = "productsCache", key = "'all'")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Lấy thông tin 1 sản phẩm theo id từ cache (nếu có).
    @Override
    @Cacheable(value = "productsCache", key = "#id")
    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    // Tạo mới sản phẩm và cập nhật cache.
    @Override
    @CachePut(value = "productsCache", key = "#result.productId")
    public String createProduct(ProductRequest product) {
        Product newProduct = Product.builder()
                .productName(product.getProductName())
                .productPrice(product.getProductPrice())
                .productDetails(product.getProductDetails())
                .build();
        productRepository.save(newProduct);
        return "productRepository.save(product)";
    }

    // Cập nhật sản phẩm và đồng thời cập nhật cache.
    @Override
    @CachePut(value = "productsCache", key = "#id")
    public Product updateProduct(String id, Product productData) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        product.setProductName(productData.getProductName());
        product.setProductPrice(productData.getProductPrice());
        product.setProductDetails(productData.getProductDetails());
        return productRepository.save(product);
    }

    // Xoá sản phẩm và xoá cache tương ứng.
    @Override
    @CacheEvict(value = "productsCache", key = "#id")
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }
}
