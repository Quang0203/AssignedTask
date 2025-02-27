package cviettel.loginservice.service;

import cviettel.loginservice.dto.request.LoginRequest;
import cviettel.loginservice.dto.response.LoginResponse;
import cviettel.loginservice.dto.response.common.ObjectResponse;

import java.time.Instant;

public interface AuthService {
    public ObjectResponse<LoginResponse, Instant> login(LoginRequest loginRequest);
    public ObjectResponse<LoginResponse, Instant> refreshToken(String refreshToken);
}
