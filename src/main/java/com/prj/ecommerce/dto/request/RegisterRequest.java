package com.prj.ecommerce.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequest {
    @NotBlank
    private String fullName;
    @NotBlank
    private String username;
    @NotBlank
    @Size(min = 6, max = 20)
    private String password;
    @NotBlank
    @Pattern(regexp = "^(0[0-9]{9})$")
    private String phoneNumber;
    @Email
    private String email;
}
