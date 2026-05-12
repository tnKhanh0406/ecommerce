package com.prj.ecommerce.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequest {
    @NotBlank(message = "Can not leave full name blank")
    private String fullName;
    @NotBlank(message = "Can not leave username blank")
    private String username;
    @NotBlank(message = "Can not leave password blank")
    @Size(min = 6, max = 20, message = "Password must have 6 to 20 characters")
    private String password;
    @NotBlank(message = "Can not leave phone number blank")
    @Pattern(regexp = "^(0[0-9]{9})$", message = "Phone number is not valid")
    private String phoneNumber;
    @Email(message = "Email is not valid")
    private String email;
}
