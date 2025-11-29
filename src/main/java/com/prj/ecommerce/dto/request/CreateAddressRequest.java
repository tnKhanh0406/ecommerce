package com.prj.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAddressRequest {
    @NotBlank
    private String receiverName;

    @Pattern(regexp = "^(0[0-9]{9})$")
    private String receiverPhone;

    @NotBlank
    private String address;

    private Integer isDefault;
}
