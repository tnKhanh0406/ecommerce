package com.prj.ecommerce.service;

import com.prj.ecommerce.common.Status;
import com.prj.ecommerce.dto.request.ChangePasswordRequest;
import com.prj.ecommerce.dto.request.LoginRequest;
import com.prj.ecommerce.dto.request.RegisterRequest;
import com.prj.ecommerce.dto.request.UpdateProfileRequest;
import com.prj.ecommerce.dto.response.UserResponse;
import com.prj.ecommerce.entity.UserEntity;
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
    UserEntity getUserByUsername(String username);
}
