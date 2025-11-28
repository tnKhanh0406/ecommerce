package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.ChangePasswordRequest;
import com.prj.ecommerce.dto.request.LoginRequest;
import com.prj.ecommerce.dto.request.RegisterRequest;
import com.prj.ecommerce.dto.request.UpdateProfileRequest;
import com.prj.ecommerce.dto.response.UserResponse;
import com.prj.ecommerce.entity.UserEntity;

public interface UserService {
    String verifyUser(LoginRequest loginRequest);
    UserResponse registerUser(RegisterRequest registerRequest);
    void changePassword(ChangePasswordRequest changePasswordRequest);
    UserResponse updateUser(UpdateProfileRequest updateProfileRequest);
}
