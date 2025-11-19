package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.LoginDTO;
import com.prj.ecommerce.dto.RegisterDTO;
import com.prj.ecommerce.entity.UserEntity;

public interface UserService {
    String verifyUser(LoginDTO loginDTO);
    UserEntity registerUser(RegisterDTO registerDTO);
}
