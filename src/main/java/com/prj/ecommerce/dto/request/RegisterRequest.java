package com.prj.ecommerce.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequest {
    private String fullName;
    private String username;
    private String password;
    private String phoneNumber;
    private String email;
}
