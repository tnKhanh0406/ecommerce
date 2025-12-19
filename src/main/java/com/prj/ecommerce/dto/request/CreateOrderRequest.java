package com.prj.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    @NotNull
    private Long addressId;

    private String note;

    @NotBlank
    private String paymentMethod;

    @NotEmpty
    List<Long> cartItemIds;

    private Long voucherId;
}
