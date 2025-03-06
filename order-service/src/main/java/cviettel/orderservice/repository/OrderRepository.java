package cviettel.orderservice.repository;

import cviettel.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    Optional<Order> findByOrderId(String id);

    List<Order> findByUserId(String userId);
}
