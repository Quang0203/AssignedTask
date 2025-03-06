package cviettel.orderservice.controller;

import cviettel.orderservice.dto.request.NewOrderRequest;
import cviettel.orderservice.entity.Order;
import cviettel.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Lấy danh sách đơn hàng
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<Order>> getOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // Tạo đơn hàng mới
    @PostMapping("/new-order")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<String> createOrder(@RequestBody NewOrderRequest order) {
//        Order newOrder = orderService.createOrder(order);
        return ResponseEntity.ok(orderService.createOrderWithProducts(order));
    }

    // Cập nhật đơn hàng
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<Order> updateOrder(@PathVariable String id, @RequestBody Order order) throws BadRequestException {
        Order updatedOrder = orderService.updateOrder(id, order);
        return ResponseEntity.ok(updatedOrder);
    }

    // Xoá đơn hàng
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
