package cviettel.loginservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "user_session", schema ="login-service")
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

    @Column(name = "token")
    private String token;

    @Column(name = "created_at")
    private Instant createdAt;

    public String getUserSessionId() {
        return userSessionId;
    }

    public void setUserSessionId(String userSessionId) {
        this.userSessionId = userSessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
