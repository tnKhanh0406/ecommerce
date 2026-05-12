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
public class OrderListResponse {
    private List<OrderResponse> orders;

    public static OrderListResponse fromEntity(List<OrderEntity> orders) {
        return new OrderListResponse(
                orders.stream().map(OrderResponse::fromEntity).collect(Collectors.toList())
        );
    }
}
