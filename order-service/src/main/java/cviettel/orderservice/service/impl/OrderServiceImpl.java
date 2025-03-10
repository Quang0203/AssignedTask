package cviettel.orderservice.service.impl;

import cviettel.orderservice.dto.request.NewOrderRequest;
import cviettel.orderservice.dto.request.OrderProductRequest;
import cviettel.orderservice.dto.request.UpdateOrderRequest;
import cviettel.orderservice.entity.Order;
import cviettel.orderservice.enums.Status;
import cviettel.orderservice.repository.OrderRepository;
import cviettel.orderservice.service.JwtService;
import cviettel.orderservice.service.OrderCacheService;
import cviettel.orderservice.service.OrderProductService;
import cviettel.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final OrderProductService orderProductService;

    private final OrderCacheService orderCacheService;

    private final RedisTemplate<String, Object> redisTemplate;

    private final JwtService jwtService;

    // Lấy danh sách order. Nếu chưa có trong cache, load từ DB và cache lại.
    @Override
    @Cacheable(value = "ordersCache", key = "'all'")
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Lấy 1 order theo id, sử dụng cache theo key là id.
    @Override
    @Cacheable(value = "ordersCache", key = "#id")
    public Order getOrderById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    @Override
    @Cacheable(value = "ordersCache", key = "#userId")
    public List<Order> getAllOdersByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }

    // Tạo mới order và cập nhật cache với key là orderId của order tạo ra.
//    @Override
//    @CachePut(value = "ordersCache", key = "#result.orderId")
//    public Order createOrder(Order order) {
//        return orderRepository.save(order);
//    }

    // Cập nhật order và đồng thời cập nhật cache.
    @Override
    @CachePut(value = "ordersCache", key = "#id")
    public Order updateOrder(String id, UpdateOrderRequest orderData) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        order.setOrderDetails(orderData.getOrderDetails());
        order.setStatus(orderData.getStatus());
        // Cập nhật thêm các thông tin cần thiết khác nếu có...
        return orderRepository.save(order);
    }

    // Xoá order và xoá cache tương ứng.
    @Override
    @CacheEvict(value = "ordersCache", key = "#id")
    public void deleteOrder(String id) {
        orderRepository.deleteById(id);
    }

    @Override
    public String createOrderWithProducts(NewOrderRequest newOrderRequest) {

        String userId = jwtService.extractUsername(redisTemplate.opsForValue().get("TokenLogin").toString());
        // Tạo mới Order
        Order order = Order.builder()
                .userId(userId)
                .orderDetails(newOrderRequest.getOrderDetails())
                .status(Status.CONFIRMED)
                .build();
        // Gọi phương thức tạo Order có @CachePut để tự động cache Order mới
        order = orderCacheService.createOrder(order);

        // Với mỗi sản phẩm trong request, tạo OrderProduct (với auto cache nếu sử dụng @CachePut trong service tương ứng)
        for (OrderProductRequest opr : newOrderRequest.getOrderProducts()) {
            orderProductService.createOrderProduct(opr, order.getOrderId());
        }

        return "Tạo đơn hàng thành công";
    }
}
