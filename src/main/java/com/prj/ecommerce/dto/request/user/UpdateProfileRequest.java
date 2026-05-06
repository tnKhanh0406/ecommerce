package com.prj.ecommerce.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @NotBlank
    private String fullName;

    @Email
    private String email;

    @Pattern(regexp = "^(0[0-9]{9})$")
    private String phoneNumber;
}
