package com.prj.ecommerce.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateShopRequest {
    private String shopName;
    private String description;
    private String logoUrl;
}
