package cviettel.productservice.service;

import cviettel.productservice.dto.request.ProductRequest;
import cviettel.productservice.entity.Product;

import java.util.List;

public interface ProductService {

    public List<Product> getAllProducts();
    public Product getProductById(String id);
    public String createProduct(ProductRequest product);
    public Product updateProduct(String id, Product product);
    public void deleteProduct(String id);

}
