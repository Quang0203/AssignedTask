package cviettel.orderservice.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderProductRequest {

    private String productId;

    private int quantity;
}
