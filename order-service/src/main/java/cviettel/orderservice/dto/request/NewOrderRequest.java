package cviettel.orderservice.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NewOrderRequest {

    private String orderDetails;

    private List<OrderProductRequest> orderProducts;

}
