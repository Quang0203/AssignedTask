package cviettel.productservice.entity;

import cviettel.productservice.entity.common.AuditTable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products", schema = "product-service")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product extends AuditTable {

    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_price")
    private String productPrice;

    @Column(name = "product_details")
    private String productDetails;

}
