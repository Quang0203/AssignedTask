package cviettel.orderservice.service;

import cviettel.orderservice.dto.request.NewOrderRequest;
import cviettel.orderservice.dto.request.UpdateOrderRequest;
import cviettel.orderservice.entity.Order;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface OrderService {

    public List<Order> getAllOrders();

    public Order getOrderById(String id);

    List<Order> getAllOdersByUserId(String userId);

//    public Order createOrder(Order order);

    public Order updateOrder(String id, UpdateOrderRequest orderData) throws BadRequestException;

    public void deleteOrder(String id);

    String createOrderWithProducts(NewOrderRequest newOrderRequest);
}
