package cviettel.orderservice.service.impl;

import cviettel.orderservice.entity.Order;
import cviettel.orderservice.repository.OrderRepository;
import cviettel.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ORDER_CACHE_KEY = "ORDER_LIST";

    public List<Order> getAllOrders() {
        // Kiểm tra cache
        List<Order> orders = (List<Order>) redisTemplate.opsForValue().get(ORDER_CACHE_KEY);
        if(orders == null) {
            orders = orderRepository.findAll();
            redisTemplate.opsForValue().set(ORDER_CACHE_KEY, orders, 30, TimeUnit.MINUTES);
        }
        return orders;
    }

    public Order createOrder(Order order) {
        Order newOrder = orderRepository.save(order);
        // Xoá cache để cập nhật danh sách mới
        redisTemplate.delete(ORDER_CACHE_KEY);
        return newOrder;
    }

    public Order updateOrder(String id, Order orderData) throws BadRequestException {
        Optional<Order> orderFind = orderRepository.findByOrderId(id);

        if(orderFind.isEmpty()) {
            throw new BadRequestException("Order not found");
        }

        Order order = orderFind.get();

        order.setOrderDetails(orderData.getOrderDetails());
        Order updatedOrder = orderRepository.save(order);
        redisTemplate.delete(ORDER_CACHE_KEY);
        return updatedOrder;
    }

    public void deleteOrder(String id) {
        orderRepository.deleteById(id);
        redisTemplate.delete(ORDER_CACHE_KEY);
    }
}
