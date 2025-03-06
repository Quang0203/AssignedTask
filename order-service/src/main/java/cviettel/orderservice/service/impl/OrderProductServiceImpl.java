package cviettel.orderservice.service.impl;

import cviettel.orderservice.dto.request.OrderProductRequest;
import cviettel.orderservice.entity.OrderProduct;
import cviettel.orderservice.repository.OrderProductRepository;
import cviettel.orderservice.service.OrderProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderProductServiceImpl implements OrderProductService {

    private final OrderProductRepository orderProductRepository;

    @Override
    @CachePut(value = "orderProductsCache", key = "#result.orderProductId")
    public OrderProduct createOrderProduct(OrderProductRequest request, String orderId) {
        OrderProduct orderProduct = OrderProduct.builder()
                .orderId(orderId)
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .build();
        return orderProductRepository.save(orderProduct);
    }
}
