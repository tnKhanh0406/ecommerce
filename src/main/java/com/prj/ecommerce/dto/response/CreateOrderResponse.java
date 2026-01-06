package com.prj.ecommerce.dto.response;

import com.prj.ecommerce.common.DiscountType;
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
    private String paymentMethod;
    private String receiverAddress;
    private String receiverPhone;
    private String receiverName;
    private Long shopId;
    private String shopName;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal totalPrice;
    private String voucherCode;
    private BigDecimal voucherDiscount;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal voucherMaxDiscount;
    private List<CreateOrderItemResponse> items;
    private List<OrderHistoryResponse> histories;

    public static CreateOrderResponse fromEntity(OrderEntity entity) {
        CreateOrderResponse response = new CreateOrderResponse();
        response.setId(entity.getId());
        response.setCreatedAt(entity.getCreatedAt());
        response.setNote(entity.getNote());
        response.setOrderStatus(entity.getOrderStatus().toString());
        response.setPaymentStatus(entity.getPaymentStatus().toString());
        response.setPaymentMethod(entity.getPaymentMethod());
        response.setReceiverAddress(entity.getReceiverAddress());
        response.setReceiverPhone(entity.getReceiverPhone());
        response.setReceiverName(entity.getReceiverName());
        response.setShopId(entity.getShopId());
        response.setShopName(entity.getShopName());
        response.setSubtotal(entity.getSubTotal());
        response.setShippingFee(entity.getShippingFee());
        response.setTotalPrice(entity.getTotal());
        response.setVoucherCode(entity.getVoucherCode());
        response.setVoucherDiscount(entity.getVoucherDiscount());
        response.setDiscountType(entity.getDiscountType());
        response.setDiscountValue(entity.getDiscountValue());
        response.setVoucherMaxDiscount(entity.getVoucherMaxDiscount());

        List<CreateOrderItemResponse> items = entity.getOrderItems().stream()
                .map(CreateOrderItemResponse::fromEntity)
                .toList();

        List<OrderHistoryResponse> histories = entity.getStatusHistories().stream()
                .map(OrderHistoryResponse::fromEntity)
                .toList();
        response.setItems(items);
        response.setHistories(histories);
        return response;
    }
}
