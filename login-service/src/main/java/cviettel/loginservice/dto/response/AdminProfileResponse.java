package cviettel.loginservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminProfileResponse {

    private String email;

    private String name;
}
