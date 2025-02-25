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

    @Column(name = "token", length = 2500)
    private String token;

    @Column(name = "refresh_token", length = 1000)
    private String refreshToken;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "refresh_count")
    private int refreshCount;

    public UserSession() {
    }

    public UserSession(String userId, String token, String refreshToken, Instant createdAt, int refreshCount) {
        this.userId = userId;
        this.token = token;
        this.refreshToken = refreshToken;
        this.createdAt = createdAt;
        this.refreshCount = refreshCount;
    }

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

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public int getRefreshCount() {
        return refreshCount;
    }

    public void setRefreshCount(int refreshCount) {
        this.refreshCount = refreshCount;
    }
}
