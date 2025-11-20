package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.LoginRequest;
import com.prj.ecommerce.dto.request.RegisterRequest;
import com.prj.ecommerce.entity.UserEntity;

public interface UserService {
    String verifyUser(LoginRequest loginRequest);
    UserEntity registerUser(RegisterRequest registerRequest);
}
