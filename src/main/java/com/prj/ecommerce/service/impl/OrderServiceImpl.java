package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.dto.request.CreateOrderRequest;
import com.prj.ecommerce.dto.response.CreateOrderItemResponse;
import com.prj.ecommerce.dto.response.CreateOrderListResponse;
import com.prj.ecommerce.dto.response.CreateOrderResponse;
import com.prj.ecommerce.entity.*;
import com.prj.ecommerce.exception.BadRequestException;
import com.prj.ecommerce.model.UserPrincipal;
import com.prj.ecommerce.repository.*;
import com.prj.ecommerce.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

//    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserAddressRepository userAddressRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    private UserEntity getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private Long getCurrentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal())
                .getUserEntity().getId();
    }

    @Override
    public CreateOrderListResponse getOrders(String keyword, OrderStatus status) {
        List<OrderEntity> orderEntities;
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasStatus = status != null;
        if (hasKeyword && !hasStatus) {
            orderEntities = orderRepository.searchOrders(keyword, getCurrentUserId());
        } else if (!hasKeyword && hasStatus) {
            orderEntities = orderRepository.findAllByUser_IdAndOrderStatusOrderByCreatedAtDesc(getCurrentUserId(), status);
        } else {
            orderEntities = orderRepository.findAllByUser_IdOrderByCreatedAtDesc(getCurrentUserId());
        }
        return CreateOrderListResponse.fromEntity(orderEntities);
    }

    @Override
    public CreateOrderResponse getOrderItems(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(getCurrentUserId())) {
            throw new AccessDeniedException("This order is not belong to the current user");
        }
        return CreateOrderResponse.fromEntity(order);
    }

    @Override
    @Transactional
    public CreateOrderListResponse createOrder(CreateOrderRequest request) {

        Long userId = getCurrentUserId();
        List<CreateOrderResponse> orderResponses = new ArrayList<>();

        // 1. Lấy cart items
        List<CartItemEntity> cartItems =
                cartItemRepository.findAllByIdInAndCart_User_Id(request.getCartItemIds(), userId);

        if (cartItems.size() != request.getCartItemIds().size()) {
            throw new BadRequestException("Some cartItemIds do not match");
        }

        // 2. Group theo shop
        Map<Long, List<CartItemEntity>> itemsByShop =
                cartItems.stream().collect(Collectors.groupingBy(CartItemEntity::getShopId));

        // 3. Lấy address
        UserAddressEntity address = userAddressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new EntityNotFoundException("UserAddress not found"));

        // 4. Xử lý từng shop để tạo nhiều order
        for (Map.Entry<Long, List<CartItemEntity>> entry : itemsByShop.entrySet()) {

            Long shopId = entry.getKey();
            List<CartItemEntity> items = entry.getValue();

            // Tạo Order
            OrderEntity order = new OrderEntity();
            order.setUser(getCurrentUser());
            order.setShopId(shopId);
            order.setNote(request.getNote());
            order.setPaymentMethod(request.getPaymentMethod());
            order.setReceiverAddress(address.getAddress());
            order.setReceiverName(address.getReceiverName());
            order.setReceiverPhone(address.getReceiverPhone());

            BigDecimal subTotal = items.stream()
                    .map(i -> i.getProductVariant().getPrice()
                            .multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            order.setSubTotal(subTotal);
            BigDecimal shippingFee = calculateShippingFee(shopId, address);
            order.setShippingFee(shippingFee);
            order.setTotal(subTotal.add(shippingFee));

            orderRepository.save(order);

            // Chuẩn bị batch save
            List<OrderItemEntity> orderItems = new ArrayList<>();
            List<ProductVariantEntity> updatedVariants = new ArrayList<>();

            // Response item list
            List<CreateOrderItemResponse> itemResponses = new ArrayList<>();

            // Tạo OrderItem
            for (CartItemEntity cartItem : items) {

                ProductVariantEntity variant = cartItem.getProductVariant();
                ProductEntity product = variant.getProduct();

                if (cartItem.getQuantity() > variant.getStock()) {
                    throw new BadRequestException("Product not enough stock");
                }

                OrderItemEntity orderItem = new OrderItemEntity();
                orderItem.setOrder(order);
                orderItem.setProductId(product.getId());
                orderItem.setProductName(product.getName());
                orderItem.setProductVariantName(getProductVariantName(variant));
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPrice(variant.getPrice());
                orderItem.setProductVariant(variant);
                orderItem.setTotalPrice(
                        variant.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
                );

                if (variant.getImages() != null && !variant.getImages().isEmpty()) {
                    orderItem.setImageUrl(variant.getImages().get(0).getImageUrl());
                } else if (product.getImages() != null && !product.getImages().isEmpty()) {
                    orderItem.setImageUrl(product.getImages().get(0).getImageUrl());
                }

                orderItems.add(orderItem);

                // Build item response
                itemResponses.add(CreateOrderItemResponse.fromEntity(orderItem));

                // Update stock
                variant.setStock(variant.getStock() - cartItem.getQuantity());
                product.setSoldCount(product.getSoldCount() + cartItem.getQuantity());
                updatedVariants.add(variant);
            }

            // Save all
            orderItemRepository.saveAll(orderItems);
            productVariantRepository.saveAll(updatedVariants);

            // Build OrderResponse
            CreateOrderResponse orderResponse = CreateOrderResponse.fromEntity(order);
            orderResponse.setItems(itemResponses);
            orderResponses.add(orderResponse);
        }

        // 5. Xóa cart items sau khi đã xử lý
        cartItemRepository.deleteAll(cartItems);

        // 6. Trả response tổng
        return new CreateOrderListResponse(orderResponses);
    }

    @Override
    public CreateOrderResponse cancelOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(getCurrentUserId())) {
            throw new AccessDeniedException("This order is not belong to the current user");
        }
        if (!order.getOrderStatus().equals(OrderStatus.PENDING)) {
            throw new BadRequestException("Order cannot be canceled");
        }
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        List<ProductVariantEntity> updatedVariants = new ArrayList<>();
        for (OrderItemEntity item : order.getOrderItems()) {
            ProductVariantEntity variant = item.getProductVariant();
            variant.setStock(variant.getStock() + item.getQuantity());
            updatedVariants.add(variant);

            ProductEntity product = variant.getProduct();
            product.setSoldCount(product.getSoldCount() - item.getQuantity());
        }
        productVariantRepository.saveAll(updatedVariants);
        return CreateOrderResponse.fromEntity(order);
    }


    private BigDecimal calculateShippingFee(Long shopId, UserAddressEntity address) {
        return BigDecimal.valueOf(25000);
    }

    private String getProductVariantName(ProductVariantEntity productVariantEntity) {
        StringBuilder sb = new StringBuilder();
        for (ProductVariantAttributeValueEntity avv : productVariantEntity.getAttributes()) {
            sb.append(avv.getAttributeValue().getProductAttribute().getName()).append(": ").append(avv.getDisplayName()).append(" ");
        }
        return sb.toString().trim();
    }

}
