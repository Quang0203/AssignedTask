package cviettel.orderservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "orders", schema = "order-service")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String orderId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "order_details")
    private String orderDetails;

    @Column(name = "created-by", updatable = false)
    private String createdBy;

    @Column(name = "created-at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated-by", updatable = false)
    private String updatedBy;

    @Column(name = "updated-at", updatable = false)
    private Instant updatedAt;
}
