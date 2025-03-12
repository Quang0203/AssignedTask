package cviettel.orderservice.service.impl;

import cviettel.orderservice.dto.request.NewOrderRequest;
import cviettel.orderservice.dto.request.OrderProductRequest;
import cviettel.orderservice.dto.request.UpdateOrderRequest;
import cviettel.orderservice.entity.Order;
import cviettel.orderservice.enums.MessageCode;
import cviettel.orderservice.enums.Status;
import cviettel.orderservice.exception.handler.BadRequestAlertException;
import cviettel.orderservice.exception.handler.NotFoundAlertException;
import cviettel.orderservice.repository.OrderRepository;
import cviettel.orderservice.service.JwtService;
import cviettel.orderservice.service.OrderCacheService;
import cviettel.orderservice.service.OrderProductService;
import cviettel.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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

    // Inject CacheManager để thao tác với cache theo Spring Cache abstraction
    private final CacheManager cacheManager;

    // Lấy danh sách order. Nếu chưa có trong cache, load từ DB và cache lại.
    @Override
    @Cacheable(value = "ordersAllCache")
    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        if (orders.isEmpty()) {
            throw new NotFoundAlertException(MessageCode.MSG1002);
        }
        return orders;
    }

    // Lấy 1 order theo id, sử dụng cache theo key là id.
    @Override
    @Cacheable(value = "ordersCache", key = "#id")
    public Order getOrderById(String id) {
        if (id == null || id.isBlank()) {
            throw new BadRequestAlertException(MessageCode.MSG1003);
        }
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(MessageCode.MSG1004));
    }

    @Override
    @Cacheable(value = "ordersCache", key = "#userId")
    public List<Order> getAllOdersByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BadRequestAlertException(MessageCode.MSG1005);
        }
        List<Order> orders = orderRepository.findByUserId(userId);
        if (orders.isEmpty()) {
            throw new NotFoundAlertException(MessageCode.MSG1002);
        }
        return orders;
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
    @CacheEvict(value = "ordersAllCache")
    public Order updateOrder(String id, UpdateOrderRequest orderData) {

        if (id.isBlank()) {
            throw new BadRequestAlertException(MessageCode.MSG1003);
        }
        if (orderData == null) {
            throw new BadRequestAlertException(MessageCode.MSG1006);
        }

        if (orderData.getOrderProducts() == null || orderData.getOrderProducts().isEmpty()) {
            throw new BadRequestAlertException(MessageCode.MSG1007);
        }

        // Validate từng sản phẩm trong request
        validateOrderProduct(orderData.getOrderProducts());

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(MessageCode.MSG1004));
        // Optionally clean và validate orderDetails
        if (orderData.getOrderDetails() != null) {
            order.setOrderDetails(orderData.getOrderDetails().trim());
        }
        // Validate và cập nhật trạng thái nếu cần thiết
        if (orderData.getStatus() == null) {
            throw new BadRequestAlertException(MessageCode.MSG1008);
        }
        order.setStatus(orderData.getStatus());
        // Cập nhật thêm các thông tin cần thiết khác nếu có...
        return orderRepository.save(order);
    }

    // Xoá order và xoá cache tương ứng.
    @Override
    @Caching(evict = {
            @CacheEvict(value = "ordersCache", key = "#id"),
            @CacheEvict(value = "ordersAllCache")
    })
    public void deleteOrder(String id) {
        if(id.isBlank()) {
            throw new BadRequestAlertException(MessageCode.MSG1003);
        }
        if (!orderRepository.existsById(id)) {
            throw new NotFoundAlertException(MessageCode.MSG1004);
        }
        orderRepository.deleteById(id);
    }

    @Override
    public String createOrderWithProducts(NewOrderRequest newOrderRequest) {

        // Validate request tổng
        if (newOrderRequest == null) {
            throw new BadRequestAlertException(MessageCode.MSG1006);
        }
        if (newOrderRequest.getOrderProducts() == null || newOrderRequest.getOrderProducts().isEmpty()) {
            throw new BadRequestAlertException(MessageCode.MSG1007);
        }

        // Validate từng sản phẩm trong request
        validateOrderProduct(newOrderRequest.getOrderProducts());

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

    private void validateOrderProduct(List<OrderProductRequest> orderProducts) {
        for (OrderProductRequest opr : orderProducts) {
            if (opr.getProductId() == null || opr.getProductId().isBlank()) {
                throw new BadRequestAlertException(MessageCode.MSG1009);
            }
            if (opr.getQuantity() <= 0) {
                throw new BadRequestAlertException(MessageCode.MSG1010);
            }
        }
    }
}
