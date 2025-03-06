package cviettel.productservice.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductRequest {

    private String productName;

    private String productPrice;

    private String productDetails;

}
