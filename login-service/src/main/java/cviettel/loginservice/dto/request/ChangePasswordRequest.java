package cviettel.loginservice.dto.request;

public class ChangePasswordRequest {
    private String username;
    private String newPassword;

    // Getters và Setters
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getNewPassword() {
        return newPassword;
    }
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
