package com.prj.ecommerce.dto.response.order;

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
public class OrderResponse {
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
    private List<OrderItemResponse> items;
    private List<OrderHistoryResponse> histories;

    public static OrderResponse fromEntity(OrderEntity entity) {
        OrderResponse res = new OrderResponse();

        res.setId(entity.getId());
        res.setCreatedAt(entity.getCreatedAt());
        res.setNote(entity.getNote());
        res.setOrderStatus(entity.getOrderStatus().toString());
        res.setPaymentStatus(entity.getPaymentStatus().toString());
        res.setPaymentMethod(entity.getPaymentMethod());
        res.setReceiverAddress(entity.getReceiverAddress());
        res.setReceiverPhone(entity.getReceiverPhone());
        res.setReceiverName(entity.getReceiverName());
        res.setShopId(entity.getShopId());
        res.setShopName(entity.getShopName());
        res.setSubtotal(entity.getSubTotal());
        res.setShippingFee(entity.getShippingFee());
        res.setTotalPrice(entity.getTotal());
        res.setVoucherCode(entity.getVoucherCode());
        res.setVoucherDiscount(entity.getVoucherDiscount());
        res.setDiscountType(entity.getDiscountType());
        res.setDiscountValue(entity.getDiscountValue());
        res.setVoucherMaxDiscount(entity.getVoucherMaxDiscount());

        return res;
    }
}
