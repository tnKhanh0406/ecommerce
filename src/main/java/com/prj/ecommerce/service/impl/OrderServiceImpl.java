package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.common.DiscountType;
import com.prj.ecommerce.common.OrderStatus;
import com.prj.ecommerce.common.UserRole;
import com.prj.ecommerce.dto.request.CreateOrderRequest;
import com.prj.ecommerce.dto.response.CreateOrderItemResponse;
import com.prj.ecommerce.dto.response.CreateOrderListResponse;
import com.prj.ecommerce.dto.response.CreateOrderResponse;
import com.prj.ecommerce.entity.*;
import com.prj.ecommerce.exception.BadRequestException;
import com.prj.ecommerce.model.UserPrincipal;
import com.prj.ecommerce.repository.*;
import com.prj.ecommerce.service.OrderService;
import com.prj.ecommerce.utils.VariantUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserAddressRepository userAddressRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final VoucherRepository voucherRepository;

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
        List<CreateOrderResponse> orderResponses = createOrdersForShops(itemsByShop, address, request);

        // 5. Xóa cart items sau khi đã xử lý
        cartItemRepository.deleteAll(cartItems);

        // 6. Trả response tổng
        return new CreateOrderListResponse(orderResponses);
    }

    @Override
    @Transactional
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

        OrderStatusHistoryEntity orderStatusHistory = addInitialStatusHistory(
                order,
                order.getOrderStatus(),
                OrderStatus.CANCELLED,
                UserRole.CUSTOMER
        );
        order.getStatusHistories().add(orderStatusHistory);

        orderRepository.save(order);
        List<ProductVariantEntity> updatedVariants = new ArrayList<>();
        for (OrderItemEntity item : order.getOrderItems()) {
            ProductVariantEntity variant = productVariantRepository.findByIdForUpdate(item.getProductVariant().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Product variant not found"));
            variant.setStock(variant.getStock() + item.getQuantity());
            updatedVariants.add(variant);

            ProductEntity product = variant.getProduct();
            product.setSoldCount(product.getSoldCount() - item.getQuantity());
        }
        productVariantRepository.saveAll(updatedVariants);
        return CreateOrderResponse.fromEntity(order);
    }

    @Transactional
    protected List<CreateOrderResponse> createOrdersForShops(
            Map<Long, List<CartItemEntity>> itemsByShop,
            UserAddressEntity address,
            CreateOrderRequest request) {
        List<CreateOrderResponse> orderResponses = new ArrayList<>();
        for (Map. Entry<Long, List<CartItemEntity>> entry : itemsByShop.entrySet()) {
            Long shopId = entry.getKey();
            List<CartItemEntity> items = entry.getValue();
            CreateOrderResponse orderResponse = createOrderForShop(shopId, items, address, request);
            orderResponses.add(orderResponse);
        }

        return orderResponses;
    }

    @Transactional
    protected CreateOrderResponse createOrderForShop(
            Long shopId,
            List<CartItemEntity> items,
            UserAddressEntity address,
            CreateOrderRequest request) {
        Long voucherId = request.getShopVouchers() != null
                ? request.getShopVouchers().get(shopId)
                : null;
        VoucherEntity voucher = applyVoucher(voucherId, shopId);
        // Tạo Order entity
        OrderEntity order = buildOrderEntity(shopId, items, address, voucher, request);

        // Tạo order status history
        addInitialStatusHistory(order, null, OrderStatus.PENDING, UserRole.CUSTOMER);

        // Save order
        orderRepository.save(order);

        //Save VoucherUsage
        if (voucher != null) {
            addVoucherUsageHistory(order, voucher);
            voucherRepository.save(voucher);
        }

        // Tạo và save order items
        List<CreateOrderItemResponse> itemResponses = createOrderItems(order, items);

        // Build response
        CreateOrderResponse orderResponse = CreateOrderResponse.fromEntity(order);
        orderResponse.setItems(itemResponses);

        return orderResponse;
    }

    private OrderEntity buildOrderEntity(
            Long shopId,
            List<CartItemEntity> items,
            UserAddressEntity address,
            VoucherEntity voucher,
            CreateOrderRequest request) {
        OrderEntity order = new OrderEntity();
        order.setUser(getCurrentUser());
        order.setShopId(shopId);
        order.setNote(request.getNote());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setReceiverAddress(address. getAddress());
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());

        // Calculate pricing
        BigDecimal subTotal = calculateSubTotal(items);
        BigDecimal shippingFee = calculateShippingFee(shopId, address);
        BigDecimal voucherDiscount = BigDecimal.ZERO;

        if (voucher != null) {
            order.setVoucher(voucher);
            order.setDiscountType(voucher.getDiscountType());
            order.setDiscountValue(voucher.getDiscountValue());
            order.setVoucherMaxDiscount(voucher.getMaxDiscount());
            order.setVoucherCode(voucher.getCode());
            voucherDiscount = calculateDiscount(voucher, subTotal);
        }


        order.setSubTotal(subTotal);
        order.setVoucherDiscount(voucherDiscount);
        order.setShippingFee(shippingFee);
        order.setTotal(subTotal.add(shippingFee).subtract(voucherDiscount).max(BigDecimal.ZERO));

        return order;
    }

    private VoucherEntity applyVoucher(Long voucherId, Long shopId) {
        if (voucherId != null) {
            VoucherEntity voucher = voucherRepository.findByIdForUpdate(voucherId)
                    .orElseThrow(() -> new EntityNotFoundException("Voucher not found"));

            if (!voucher.getShop().getId().equals(shopId)) {
                throw new BadRequestException("Voucher not belong to this shop");
            }
            if (voucher.getUsedCount() >= voucher.getUsageLimit()) {
                throw new BadRequestException("This voucher is out of usage");
            }
            voucher.setUsedCount(voucher.getUsedCount() + 1);
            return voucher;
        }
        return null;
    }

    private BigDecimal calculateDiscount(VoucherEntity voucher, BigDecimal subTotal) {
        if (subTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new BadRequestException("Your order does not meet voucher conditions");
        }

        BigDecimal discount;
        if (voucher.getDiscountType() == DiscountType.FIXED) {
            discount = voucher.getDiscountValue();
        } else {
            discount = subTotal
                    .multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        if (voucher.getMaxDiscount() != null) {
            discount = discount.min(voucher.getMaxDiscount());
        }

        return discount.min(subTotal);
    }


    private BigDecimal calculateSubTotal(List<CartItemEntity> items) {
        return items.stream()
                .map(i -> i.getProductVariant().getPrice()
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal:: add);
    }

    private OrderStatusHistoryEntity addInitialStatusHistory(OrderEntity order,
                                                             OrderStatus from,
                                                             OrderStatus to,
                                                             UserRole userRole) {
        OrderStatusHistoryEntity orderStatusHistory = new OrderStatusHistoryEntity();
        orderStatusHistory.setFromStatus(from);
        orderStatusHistory.setToStatus(to);
        orderStatusHistory.setChangedBy(userRole);
        orderStatusHistory.setChangedById(getCurrentUserId());
        orderStatusHistory.setOrder(order);

        order.getStatusHistories().add(orderStatusHistory);
        return orderStatusHistory;
    }

    private void addVoucherUsageHistory(OrderEntity order, VoucherEntity voucher) {
        VoucherUsageEntity voucherUsage = new VoucherUsageEntity();
        voucherUsage.setVoucher(voucher);
        voucherUsage.setUser(getCurrentUser());
        voucherUsage.setOrder(order);
        voucherUsageRepository.save(voucherUsage);
    }

    private List<CreateOrderItemResponse> createOrderItems(
            OrderEntity order,
            List<CartItemEntity> items) {

        List<OrderItemEntity> orderItems = new ArrayList<>();
        List<ProductVariantEntity> updatedVariants = new ArrayList<>();
        List<CreateOrderItemResponse> itemResponses = new ArrayList<>();

        for (CartItemEntity cartItem : items) {
            OrderItemEntity orderItem = createOrderItem(order, cartItem);
            orderItems.add(orderItem);

            // Update stock
            ProductVariantEntity variant = updateProductStock(cartItem);
            updatedVariants.add(variant);

            // Build response
            itemResponses.add(CreateOrderItemResponse.fromEntity(orderItem));
        }

        // Batch save
        orderItemRepository.saveAll(orderItems);
        productVariantRepository.saveAll(updatedVariants);

        return itemResponses;
    }

    private OrderItemEntity createOrderItem(OrderEntity order, CartItemEntity cartItem) {
        ProductVariantEntity variant = cartItem. getProductVariant();
        ProductEntity product = variant.getProduct();

        validateStock(cartItem, variant);

        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrder(order);
        orderItem.setProductId(product.getId());
        orderItem.setProductName(product.getName());
        orderItem.setProductVariantName(VariantUtil.generateVariantName(variant));
        orderItem.setQuantity(cartItem. getQuantity());
        orderItem.setPrice(variant.getPrice());
        orderItem.setProductVariant(variant);
        orderItem.setTotalPrice(
                variant.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
        );

        // Set image
        setOrderItemImage(orderItem, variant, product);

        return orderItem;
    }

    private void validateStock(CartItemEntity cartItem, ProductVariantEntity variant) {
        if (cartItem.getQuantity() > variant.getStock()) {
            throw new BadRequestException("Product not enough stock");
        }
    }

    private void setOrderItemImage(OrderItemEntity orderItem,
                                   ProductVariantEntity variant,
                                   ProductEntity product) {

        if (variant.getImages() != null && !variant.getImages().isEmpty()) {
            orderItem.setImageUrl(variant. getImages().get(0).getImageUrl());
        } else if (product.getImages() != null && !product.getImages().isEmpty()) {
            orderItem.setImageUrl(product.getImages().get(0).getImageUrl());
        }
    }

    private ProductVariantEntity updateProductStock(CartItemEntity cartItem) {
        ProductVariantEntity variant = productVariantRepository.findByIdForUpdate(cartItem.getProductVariant().getId())
                .orElseThrow(() -> new EntityNotFoundException("Product variant not found"));
        ProductEntity product = variant.getProduct();
        variant.setStock(variant.getStock() - cartItem.getQuantity());
        product.setSoldCount(product.getSoldCount() + cartItem.getQuantity());

        return variant;
    }

    private BigDecimal calculateShippingFee(Long shopId, UserAddressEntity address) {
        return BigDecimal.valueOf(25000);
    }
}
