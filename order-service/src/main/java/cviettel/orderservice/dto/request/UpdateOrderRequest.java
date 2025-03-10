package cviettel.orderservice.dto.request;

import cviettel.orderservice.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UpdateOrderRequest {

    private String orderDetails;

    private List<OrderProductRequest> orderProducts;

    private Status status;

}
