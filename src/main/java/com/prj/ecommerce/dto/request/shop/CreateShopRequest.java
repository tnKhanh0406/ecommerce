package com.prj.ecommerce.dto.request.shop;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateShopRequest {
    @NotBlank(message = "Shop name is required")
    private String shopName;

    private String description;
}
