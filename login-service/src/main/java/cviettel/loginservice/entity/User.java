package cviettel.loginservice.entity;

import cviettel.loginservice.entity.common.AuditTable;
import cviettel.loginservice.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.Instant;

@Entity
@Table(name = "user", schema = "login-service")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@DynamicUpdate
@DynamicInsert
public class User extends AuditTable {
    @Id
    @Column(name = "user_id", nullable = false)
//    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;

    @Column(name = "is_verified", length = 255)
    private String isVerified;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "name", length = 40)
    private String name;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "role", length = 10)
    private String role;
}
