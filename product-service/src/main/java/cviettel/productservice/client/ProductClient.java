package cviettel.productservice.client;

import cviettel.productservice.entity.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "${feign.client.config.product-service.name}", url = "${feign.client.config.product-service.url}")
public interface ProductClient {
    @GetMapping("{id}")
    Product getProductById(@PathVariable("id") String id);
}
