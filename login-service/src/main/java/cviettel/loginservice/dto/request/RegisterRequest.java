package cviettel.loginservice.dto.request;

import cviettel.loginservice.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {

    private String isVerified;

    private String email;

    private String password;

    private String name;

    private Status status;

    private String role;
}
