package cviettel.loginservice.service;

import cviettel.loginservice.dto.request.LoginRequest;
import cviettel.loginservice.dto.response.LoginResponse;
import cviettel.loginservice.dto.response.common.ObjectResponse;

public interface AuthService {
    public ObjectResponse<LoginResponse> login(LoginRequest loginRequest);
}
