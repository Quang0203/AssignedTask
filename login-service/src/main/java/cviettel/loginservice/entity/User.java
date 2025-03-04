package cviettel.loginservice.entity;

import cviettel.loginservice.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user", schema = "login-service")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class User {
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

    @Column(name = "created-by", updatable = false)
    private String createdBy;

    @Column(name = "created-at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated-by", updatable = false)
    private String updatedBy;

    @Column(name = "updated-at", updatable = false)
    private Instant updatedAt;
}
