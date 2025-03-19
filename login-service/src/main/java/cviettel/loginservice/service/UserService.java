package cviettel.loginservice.service;

import cviettel.loginservice.dto.request.AdminProfileRequest;
import cviettel.loginservice.dto.request.UserProfileRequest;
import cviettel.loginservice.dto.response.AdminProfileResponse;
import cviettel.loginservice.dto.response.UserProfileResponse;
import cviettel.loginservice.entity.User;

public interface UserService {

    public UserProfileResponse getUserProfile(UserProfileRequest userProfileRequest);

    public AdminProfileResponse getAdminProfile(AdminProfileRequest adminProfileRequest);
}
