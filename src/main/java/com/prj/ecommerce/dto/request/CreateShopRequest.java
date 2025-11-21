package com.prj.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateShopRequest {
    @NotBlank(message = "Shop name is required")
    private String shopName;

    private String description;

    private String logoUrl;
}
