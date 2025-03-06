package cviettel.orderservice.service;

import cviettel.orderservice.entity.Order;
import cviettel.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCacheService {

    private final OrderRepository orderRepository;
    @CachePut(value = "ordersCache", key = "#result.orderId")
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }
}
