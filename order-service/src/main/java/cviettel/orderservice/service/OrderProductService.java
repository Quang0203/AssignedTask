package cviettel.orderservice.service;

import cviettel.orderservice.dto.request.OrderProductRequest;
import cviettel.orderservice.entity.OrderProduct;

public interface OrderProductService {

    OrderProduct createOrderProduct(OrderProductRequest request, String orderId);

}
