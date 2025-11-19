package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.LoginDTO;

public interface UserService {
    String verifyUser(LoginDTO loginDTO);
}
