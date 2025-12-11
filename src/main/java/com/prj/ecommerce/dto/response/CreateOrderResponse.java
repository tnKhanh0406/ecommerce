package com.prj.ecommerce.dto.response;

import com.prj.ecommerce.entity.OrderEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {
    private Long id;
    private LocalDateTime createdAt;
    private String note;
    private String orderStatus;
    private String paymentStatus;
    private String receiverAddress;
    private String receiverPhone;
    private String receiverName;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal totalPrice;
    private List<CreateOrderItemResponse> items;

    public static CreateOrderResponse fromEntity(OrderEntity entity) {
        CreateOrderResponse response = new CreateOrderResponse();
        response.setId(entity.getId());
        response.setCreatedAt(entity.getCreatedAt());
        response.setNote(entity.getNote());
        response.setOrderStatus(entity.getOrderStatus().toString());
        response.setPaymentStatus(entity.getPaymentStatus().toString());
        response.setReceiverAddress(entity.getReceiverAddress());
        response.setReceiverPhone(entity.getReceiverPhone());
        response.setReceiverName(entity.getReceiverName());
        response.setSubtotal(entity.getSubTotal());
        response.setShippingFee(entity.getShippingFee());
        response.setTotalPrice(entity.getTotal());

        List<CreateOrderItemResponse> items = entity.getOrderItems().stream()
                .map(CreateOrderItemResponse::fromEntity)
                .toList();
        response.setItems(items);
        return response;
    }
}
