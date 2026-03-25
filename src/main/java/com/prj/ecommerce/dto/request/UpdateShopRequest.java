package com.prj.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter @Getter
@AllArgsConstructor
public class UpdateShopRequest {
    @NotBlank(message = "Shop name is required")
    private String shopName;

    private String description;

    private String logoUrl;
}
