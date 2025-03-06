package cviettel.orderservice.entity;

import cviettel.orderservice.entity.common.AuditTable;
import cviettel.orderservice.enums.Status;
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
public class Order extends AuditTable {

    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String orderId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "order_details")
    private String orderDetails;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

}
