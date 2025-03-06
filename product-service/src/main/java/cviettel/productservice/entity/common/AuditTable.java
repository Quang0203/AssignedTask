package cviettel.productservice.entity.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;


/**
 * AuditTable
 * @author minhquang
 * Time: 2024-12-06
 * @version 1.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditTable {

    @CreatedBy
    @Column(name = "created-by")
    private String createdBy;

    @CreatedDate
    @Column(name = "created-at", updatable = false)
    @JsonIgnore
    private Instant createdAt = Instant.now();

    @LastModifiedBy
    @Column(name = "updated-by")
    private String updatedBy;

    @LastModifiedDate
    @Column(name = "updated-at", updatable = false)
    @JsonIgnore
    private Instant updatedAt = Instant.now();

}
