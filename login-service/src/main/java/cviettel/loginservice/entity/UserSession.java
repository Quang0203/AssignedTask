package cviettel.loginservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "user_session", schema ="login-service")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {
    @Id
    @Column(name = "user_session_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userSessionId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "token", length = 2500)
    private String token;

    @Column(name = "refresh_token", length = 1000)
    private String refreshToken;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "refresh_count")
    private int refreshCount;

}
