package com.prj.ecommerce.service;

import com.prj.ecommerce.common.Status;
import com.prj.ecommerce.dto.request.user.ChangePasswordRequest;
import com.prj.ecommerce.dto.request.user.LoginRequest;
import com.prj.ecommerce.dto.request.user.RegisterRequest;
import com.prj.ecommerce.dto.request.user.UpdateProfileRequest;
import com.prj.ecommerce.dto.response.user.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    String verifyUser(LoginRequest loginRequest);
    UserResponse registerUser(RegisterRequest registerRequest);
    void changePassword(ChangePasswordRequest changePasswordRequest);
    UserResponse updateUser(UpdateProfileRequest updateProfileRequest);
    
    // Admin methods
    Page<UserResponse> getAllUsers(String search, Pageable pageable);
    UserResponse getUserById(Long id);
    void updateUserStatus(Long userId, Status status);
    UserResponse getUserByUsername(String username);
}
