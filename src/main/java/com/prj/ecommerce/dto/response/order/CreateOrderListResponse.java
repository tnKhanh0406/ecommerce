package com.prj.ecommerce.dto.response.order;

import com.prj.ecommerce.entity.OrderEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderListResponse {
    private List<CreateOrderResponse> orders;

    public static CreateOrderListResponse fromEntity(List<OrderEntity> orders) {
        return new CreateOrderListResponse(
                orders.stream().map(CreateOrderResponse::fromEntity).collect(Collectors.toList())
        );
    }
}
