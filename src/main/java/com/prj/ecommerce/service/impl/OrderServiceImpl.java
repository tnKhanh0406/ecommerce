package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.common.*;
import com.prj.ecommerce.dto.request.order.CreateOrderRequest;
import com.prj.ecommerce.dto.request.notification.NotificationRequest;
import com.prj.ecommerce.dto.response.order.OrderItemResponse;
import com.prj.ecommerce.dto.response.order.OrderListResponse;
import com.prj.ecommerce.dto.response.order.OrderResponse;
import com.prj.ecommerce.dto.response.order.OrderHistoryResponse;
import com.prj.ecommerce.dto.response.review.ProductReviewResponse;
import com.prj.ecommerce.dto.response.shop.ShopSalesAnalyticsResponse;
import com.prj.ecommerce.dto.response.shop.ShopTopProductResponse;
import com.prj.ecommerce.entity.*;
import com.prj.ecommerce.exception.BadRequestException;
import com.prj.ecommerce.model.UserPrincipal;
import com.prj.ecommerce.repository.*;
import com.prj.ecommerce.service.NotificationService;
import com.prj.ecommerce.service.OrderService;
import com.prj.ecommerce.service.ReviewPolicyService;
import com.prj.ecommerce.utils.VariantUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

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
    private final NotificationService notificationService;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final ReviewPolicyService reviewPolicyService;
    private final ProductReviewRepository productReviewRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final ProductImageRepository productImageRepository;

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

    // @Override
    // public CreateOrderListResponse getOrders(String keyword, OrderStatus status) {
    //     List<OrderEntity> orderEntities;
    //     boolean hasKeyword = keyword != null && !keyword.isBlank();
    //     boolean hasStatus = status != null;
    //     if (hasKeyword && !hasStatus) {
    //         orderEntities = orderRepository.searchOrders(keyword, getCurrentUserId());
    //     } else if (!hasKeyword && hasStatus) {
    //         orderEntities = orderRepository.findAllByUser_IdAndOrderStatusOrderByCreatedAtDesc(getCurrentUserId(), status);
    //     } else {
    //         orderEntities = orderRepository.findAllByUser_IdOrderByCreatedAtDesc(getCurrentUserId());
    //     }
    //     List<CreateOrderResponse> responses = orderEntities.stream()
    //             .map(order -> {
    //                 CreateOrderResponse response = CreateOrderResponse.fromEntity(order);
    //                 enrichOrderItemsWithReviewStatus(response, order);
    //                 return response;
    //             }).toList();
    //     return new CreateOrderListResponse(responses);
    // }

    @Override
    public OrderListResponse getOrders(String keyword, OrderStatus status) {

        Long userId = getCurrentUserId();

        // 1. Load orders
        List<OrderEntity> orders = getOrdersByCondition(keyword, status, userId);

        if (orders.isEmpty()) {
            return new OrderListResponse(List.of());
        }

        List<Long> orderIds = orders.stream()
                .map(OrderEntity::getId)
                .toList();

        // 2. Batch load related data
        List<OrderItemEntity> items = orderItemRepository.findByOrderIds(orderIds);
        List<OrderStatusHistoryEntity> histories = orderStatusHistoryRepository.findByOrderIds(orderIds);
        
        // Query 1: Load reviews với product, user, shop, reply
        List<ProductReviewEntity> reviews = productReviewRepository.findByOrderIds(orderIds);
        
        // Query 2: Extract review IDs và load images riêng
        List<Long> reviewIds = reviews.stream()
                .map(ProductReviewEntity::getId)
                .toList();
        List<ProductImageEntity> reviewImages = productImageRepository.findByReviewIds(reviewIds);

        // 3. Map dữ liệu
        Map<Long, List<OrderItemEntity>> itemMap =
                items.stream().collect(Collectors.groupingBy(i -> i.getOrder().getId()));

        Map<Long, List<OrderStatusHistoryEntity>> historyMap =
                histories.stream().collect(Collectors.groupingBy(h -> h.getOrder().getId()));

        Map<Long, ProductReviewEntity> reviewMap =
                reviews.stream().collect(Collectors.toMap(
                        r -> r.getOrderItem().getId(),
                        Function.identity()
                ));
        
        // Map images theo review ID
        Map<Long, List<ProductImageEntity>> reviewImageMap =
                reviewImages.stream()
                        .collect(Collectors.groupingBy(img -> img.getReview().getId()));

        // 4. Build response
        List<OrderResponse> responses = orders.stream().map(order -> {

            OrderResponse res = OrderResponse.fromEntity(order);

            List<OrderItemEntity> orderItems = itemMap.getOrDefault(order.getId(), List.of());

            boolean canReviewOrder = reviewPolicyService.canReview(order, 10);

            List<OrderItemResponse> itemResponses = orderItems.stream().map(item -> {

                ProductReviewEntity review = reviewMap.get(item.getId());

                boolean reviewed = review != null;
                boolean canReview = !reviewed && canReviewOrder;

                boolean canUpdate = false;
                if (review != null) {
                    canUpdate = reviewPolicyService.canUpdate(review, 5);
                }

                // Set images cho review từ map
                if (review != null) {
                    List<ProductImageEntity> images = reviewImageMap.getOrDefault(review.getId(), List.of());
                    review.setImages(images);
                }

                return OrderItemResponse.builder()
                        .orderItemId(item.getId())
                        .productId(item.getProductId())
                        .imageUrl(item.getImageUrl())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .productName(item.getProductName())
                        .productVariantName(item.getProductVariantName())
                        .totalPrice(item.getTotalPrice())
                        .reviewed(reviewed)
                        .canReview(canReview)
                        .canUpdate(canUpdate)
                        .reviewResponse(
                                review != null ? ProductReviewResponse.fromEntity(review) : null
                        )
                        .build();

            }).toList();

            List<OrderHistoryResponse> historyResponses =
                    historyMap.getOrDefault(order.getId(), List.of())
                            .stream()
                            .map(OrderHistoryResponse::fromEntity)
                            .toList();

            res.setItems(itemResponses);
            res.setHistories(historyResponses);

            return res;

        }).toList();

        return new OrderListResponse(responses);
    }

    @Override
    public OrderListResponse getOrdersForAdmin(String keyword, OrderStatus status) {
        List<OrderEntity> orderEntities;
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasStatus = status != null;

        if (hasKeyword && !hasStatus) {
            orderEntities = orderRepository.searchOrdersForAdmin(keyword.trim());
        } else if (!hasKeyword && hasStatus) {
            orderEntities = orderRepository.findAllByOrderStatusOrderByCreatedAtDesc(status);
        } else if (hasKeyword) {
            orderEntities = orderRepository.searchOrdersForAdmin(keyword.trim()).stream()
                    .filter(order -> order.getOrderStatus() == status)
                    .toList();
        } else {
            orderEntities = orderRepository.findAllByOrderByCreatedAtDesc();
        }

        List<OrderResponse> responses = orderEntities.stream()
                .map(order -> {
                    OrderResponse response = OrderResponse.fromEntity(order);
                    enrichOrderItemsWithReviewStatus(response, order);
                    return response;
                })
                .toList();

        return new OrderListResponse(responses);
    }

    @Override
    public List<OrderResponse> getOrdersByShopId(Long shopId, OrderStatus status) {
        UserEntity currentUser = getCurrentUser();
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));

        if (!shop.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("This shop does not belong to you");
        }

        List<OrderEntity> orderEntities;
        if (status == null) {
            orderEntities = orderRepository.findAllByShopIdOrderByCreatedAtDesc(shopId);
        } else {
            orderEntities = orderRepository.findAllByShopIdAndOrderStatusOrderByCreatedAtDesc(shopId, status);
        }

        return orderEntities.stream()
                .map(order -> {
                    OrderResponse response = OrderResponse.fromEntity(order);
                    enrichOrderItemsWithReviewStatus(response, order);
                    return response;
                })
                .toList();
    }

    @Override
    public OrderResponse getOrderItems(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(getCurrentUserId())) {
            throw new AccessDeniedException("This order is not belong to the current user");
        }
        OrderResponse response = OrderResponse.fromEntity(order);
        enrichOrderItemsWithReviewStatus(response, order);
        return response;
    }

    @Override
    public OrderResponse getOrderDetailForAdmin(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        OrderResponse response = OrderResponse.fromEntity(order);
        enrichOrderItemsWithReviewStatus(response, order);
        return response;
    }

    @Override
    @Transactional
    public OrderListResponse createOrder(CreateOrderRequest request) {

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
        List<OrderResponse> orderResponses = createOrdersForShops(itemsByShop, address, request, userId);

        // 5. Xóa cart items sau khi đã xử lý
        cartItemRepository.deleteAll(cartItems);

        // 6. Trả response tổng
        return new OrderListResponse(orderResponses);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        if (!order.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("This order does not belong to the current user");
        }
        if (!order.getOrderStatus().equals(OrderStatus.PENDING)) {
            throw new BadRequestException("Order cannot be canceled");
        }

        OrderStatus oldStatus = order.getOrderStatus();
        order.setOrderStatus(OrderStatus.CANCELLED);

        // Tạo status history
        addInitialStatusHistory(
                order,
                oldStatus,
                OrderStatus.CANCELLED,
                UserRole.CUSTOMER,
                currentUserId
        );

        // Save order TRƯỚC khi lock các entity khác
        order = orderRepository.saveAndFlush(order); // Dùng saveAndFlush để commit ngay

        // Sau đó mới update variants và products với lock
        List<ProductVariantEntity> updatedVariants = new ArrayList<>();
        List<ProductEntity> updatedProducts = new ArrayList<>();

        for (OrderItemEntity item : order.getOrderItems()) {
            ProductVariantEntity variant = productVariantRepository.findByIdForUpdate(item.getProductVariant().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Product variant not found"));
            Integer quantity = item.getQuantity();
            if (quantity == null) {
                quantity = 0;
            }
            Integer stock = variant.getStock();
            if (stock == null) stock = 0;

            variant.setStock(stock + quantity);
            updatedVariants.add(variant);

            ProductEntity product = variant.getProduct();
            Integer sold = product.getSoldCount();
            if (sold == null) sold = 0;

            product.setSoldCount(sold - quantity);
            updatedProducts.add(product);
        }

        // Save variants và products
        productVariantRepository.saveAllAndFlush(updatedVariants); // Dùng saveAllAndFlush
        productRepository.saveAllAndFlush(updatedProducts);

        // Gửi thông báo
        NotificationRequest notificationRequest = new NotificationRequest(
                "Hủy đơn hàng thành công",
                "Đơn hàng #" + order.getId() + " đã được hủy",
                NotificationType.ORDER_CANCELLED,
                order.getId(),
                order.getUser().getId(),
                ReferenceType.ORDER
        );

        notificationService.sendNotification(notificationRequest);

        return OrderResponse.fromEntity(order);
    }

    @Override
    @Transactional
    public void updateOrderStatusBySeller(Long orderId, OrderStatus newStatus) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        UserEntity currentUser = getCurrentUser();
        ShopEntity shop = shopRepository.findById(order.getShopId())
                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));

        if (!shop.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("This order does not belong to your shop");
        }

        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new BadRequestException("Completed order cannot be changed");
        }

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cancelled order cannot be changed");
        }

        if (newStatus == null) {
            throw new BadRequestException("Status is required");
        }

        if (newStatus == OrderStatus.CANCELLED) {
            throw new BadRequestException("Seller cannot set order to cancelled");
        }

        if (!isAllowedSellerTransition(order.getOrderStatus(), newStatus)) {
            throw new BadRequestException("Invalid order status transition");
        }

        OrderStatus oldStatus = order.getOrderStatus();
        order.setOrderStatus(newStatus);
        addInitialStatusHistory(
                order,
                oldStatus,
                newStatus,
                UserRole.SELLER,
                currentUser.getId()
        );

        order = orderRepository.saveAndFlush(order);

        if (newStatus == OrderStatus.COMPLETED) {
            NotificationRequest notificationRequest = new NotificationRequest(
                    "Đơn hàng đã hoàn tất",
                    "Đơn hàng #" + order.getId() + " đã được giao hoàn tất",
                    NotificationType.ORDER_COMPLETED,
                    order.getId(),
                    order.getUser().getId(),
                    ReferenceType.ORDER
            );
            notificationService.sendNotification(notificationRequest);
        }
    }

    @Override
    @Transactional
    public void updateOrderStatusByAdmin(Long orderId, OrderStatus newStatus) {
        if (newStatus == null) {
            throw new BadRequestException("Status is required");
        }

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new BadRequestException("Completed order cannot be changed");
        }

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cancelled order cannot be changed");
        }

        OrderStatus oldStatus = order.getOrderStatus();
        if (oldStatus == newStatus) {
            throw new BadRequestException("Order already has this status");
        }

        boolean validTransition = isAllowedSellerTransition(oldStatus, newStatus)
                || newStatus == OrderStatus.CANCELLED;
        if (!validTransition) {
            throw new BadRequestException("Invalid order status transition");
        }

        UserEntity currentUser = getCurrentUser();
        order.setOrderStatus(newStatus);
        addInitialStatusHistory(order, oldStatus, newStatus, UserRole.ADMIN, currentUser.getId());
        order = orderRepository.saveAndFlush(order);

        if (newStatus == OrderStatus.COMPLETED) {
            NotificationRequest notificationRequest = new NotificationRequest(
                    "Đơn hàng đã hoàn tất",
                    "Đơn hàng #" + order.getId() + " đã được giao hoàn tất",
                    NotificationType.ORDER_COMPLETED,
                    order.getId(),
                    order.getUser().getId(),
                    ReferenceType.ORDER
            );
            notificationService.sendNotification(notificationRequest);
        }

        if (newStatus == OrderStatus.CANCELLED) {
            NotificationRequest notificationRequest = new NotificationRequest(
                    "Đơn hàng đã bị hủy",
                    "Đơn hàng #" + order.getId() + " đã bị hủy bởi quản trị viên",
                    NotificationType.ORDER_CANCELLED,
                    order.getId(),
                    order.getUser().getId(),
                    ReferenceType.ORDER
            );
            notificationService.sendNotification(notificationRequest);
        }
    }

    @Override
    public ShopSalesAnalyticsResponse getShopSalesAnalytics(Long shopId, LocalDate startDate, LocalDate endDate) {
        LocalDate resolvedEndDate = endDate == null ? LocalDate.now() : endDate;
        LocalDate resolvedStartDate = startDate == null ? resolvedEndDate.minusDays(29) : startDate;

        if (resolvedStartDate.isAfter(resolvedEndDate)) {
            LocalDate temp = resolvedStartDate;
            resolvedStartDate = resolvedEndDate;
            resolvedEndDate = temp;
        }

        LocalDateTime from = resolvedStartDate.atStartOfDay();
        LocalDateTime to = resolvedEndDate.plusDays(1).atStartOfDay().minusNanos(1);

        List<OrderEntity> orders = orderRepository
                .findAllByShopIdAndCreatedAtBetweenOrderByCreatedAtDesc(shopId, from, to);

        long totalOrders = orders.size();
        long completedOrders = orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.COMPLETED).count();
        long cancelledOrders = orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.CANCELLED).count();
        long pendingOrders = orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.PENDING).count();
        long confirmedOrders = orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.CONFIRMED).count();
        long shippingOrders = orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.SHIPPING).count();

        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.COMPLETED)
                .map(OrderEntity::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = completedOrders == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(completedOrders), 2, RoundingMode.HALF_UP);

        BigDecimal cancelRate = totalOrders == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(cancelledOrders)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);

        BigDecimal completionRate = totalOrders == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(completedOrders)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);

        List<ShopTopProductResponse> topProducts = buildTopProducts(orders);

        ShopSalesAnalyticsResponse response = new ShopSalesAnalyticsResponse();
        response.setStartDate(resolvedStartDate);
        response.setEndDate(resolvedEndDate);
        response.setTotalOrders(totalOrders);
        response.setCompletedOrders(completedOrders);
        response.setCancelledOrders(cancelledOrders);
        response.setPendingOrders(pendingOrders);
        response.setConfirmedOrders(confirmedOrders);
        response.setShippingOrders(shippingOrders);
        response.setTotalRevenue(totalRevenue);
        response.setAverageOrderValue(averageOrderValue);
        response.setCancelRate(cancelRate);
        response.setCompletionRate(completionRate);
        response.setTopProducts(topProducts);

        return response;
    }

    private List<OrderEntity> getOrdersByCondition(String keyword, OrderStatus status, Long userId) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasStatus = status != null;

        if (hasKeyword && !hasStatus) {
            return orderRepository.searchOrders(keyword, userId);
        } else if (!hasKeyword && hasStatus) {
            return orderRepository.findAllByUser_IdAndOrderStatusOrderByCreatedAtDesc(userId, status);
        } else {
            return orderRepository.findAllByUser_IdOrderByCreatedAtDesc(userId);
        }
    }

    private List<ShopTopProductResponse> buildTopProducts(List<OrderEntity> orders) {
        Map<Long, TopProductAggregate> aggregateMap = new HashMap<>();

        for (OrderEntity order : orders) {
            if (order.getOrderStatus() != OrderStatus.COMPLETED) {
                continue;
            }

            Set<Long> seenInOrder = new HashSet<>();
            for (OrderItemEntity item : order.getOrderItems()) {
                TopProductAggregate aggregate = aggregateMap.computeIfAbsent(
                        item.getProductId(),
                        ignored -> new TopProductAggregate(
                                item.getProductId(),
                                item.getProductName(),
                                item.getImageUrl(),
                                0L,
                                0L,
                                BigDecimal.ZERO
                        )
                );

                aggregate.totalQuantity += item.getQuantity() == null ? 0 : item.getQuantity();
                aggregate.totalRevenue = aggregate.totalRevenue.add(
                        item.getTotalPrice() == null ? BigDecimal.ZERO : item.getTotalPrice()
                );

                if (seenInOrder.add(item.getProductId())) {
                    aggregate.orderCount += 1;
                }
            }
        }

        return aggregateMap.values().stream()
                .sorted((a, b) -> {
                    int revenueCompare = b.totalRevenue.compareTo(a.totalRevenue);
                    if (revenueCompare != 0) {
                        return revenueCompare;
                    }
                    return Long.compare(b.totalQuantity, a.totalQuantity);
                })
                .limit(10)
                .map(item -> new ShopTopProductResponse(
                        item.productId,
                        item.productName,
                        item.imageUrl,
                        item.totalQuantity,
                        item.orderCount,
                        item.totalRevenue
                ))
                .toList();
    }

    private static class TopProductAggregate {
        private final Long productId;
        private final String productName;
        private final String imageUrl;
        private Long totalQuantity;
        private Long orderCount;
        private BigDecimal totalRevenue;

        private TopProductAggregate(Long productId,
                                    String productName,
                                    String imageUrl,
                                    Long totalQuantity,
                                    Long orderCount,
                                    BigDecimal totalRevenue) {
            this.productId = productId;
            this.productName = productName;
            this.imageUrl = imageUrl;
            this.totalQuantity = totalQuantity;
            this.orderCount = orderCount;
            this.totalRevenue = totalRevenue;
        }
    }

    private void enrichOrderItemsWithReviewStatus(OrderResponse response, OrderEntity order) {
        Set<Long> reviewedItemIds = new HashSet<>(productReviewRepository
                .findReviewedOrderItemIds(order.getId()));
        boolean canReviewOrder = reviewPolicyService.canReview(order, 10);

        List<OrderItemResponse> orderItemResponses =
                order.getOrderItems().stream().map(item -> {

                    OrderItemResponse dto = OrderItemResponse.fromEntity(item);
                    boolean reviewed = reviewedItemIds.contains(item.getId());
                    boolean canReview = !reviewed && canReviewOrder;

                    boolean canUpdate = false;
                    if (item.getReview() != null) {
                        canUpdate = reviewPolicyService.canUpdate(item.getReview(), 5);
                    }

                    dto.setReviewed(reviewed);
                    dto.setCanReview(canReview);
                    dto.setCanUpdate(canUpdate);

                    return dto;
                }).toList();

        response.setItems(orderItemResponses);
    }


    @Transactional
    protected List<OrderResponse> createOrdersForShops(
            Map<Long, List<CartItemEntity>> itemsByShop,
            UserAddressEntity address,
            CreateOrderRequest request,
            Long userId) {
        List<OrderResponse> orderResponses = new ArrayList<>();
        for (Map. Entry<Long, List<CartItemEntity>> entry : itemsByShop.entrySet()) {
            Long shopId = entry.getKey();
            List<CartItemEntity> items = entry.getValue();
            OrderResponse orderResponse = createOrderForShop(shopId, items, address, request, userId);
            orderResponses.add(orderResponse);
        }

        return orderResponses;
    }

    @Transactional
    protected OrderResponse createOrderForShop(
            Long shopId,
            List<CartItemEntity> items,
            UserAddressEntity address,
            CreateOrderRequest request,
            Long userId) {
        Long voucherId = request.getShopVouchers() != null
                ? request.getShopVouchers().get(shopId)
                : null;
        VoucherEntity voucher = applyVoucher(voucherId, shopId);
        // Tạo Order entity
        OrderEntity order = buildOrderEntity(shopId, items, address, voucher, request);

        // Tạo order status history
        addInitialStatusHistory(order, null, OrderStatus.PENDING, UserRole.CUSTOMER, userId);

        // Save order
        orderRepository.save(order);

        //Save VoucherUsage
        if (voucher != null) {
            addVoucherUsageHistory(order, voucher);
            voucherRepository.save(voucher);
        }

        // Tạo và save order items
        List<OrderItemResponse> itemResponses = createOrderItems(order, items);

        //Gui thong bao
        NotificationRequest notificationRequest = new NotificationRequest(
                "Đặt hàng thành công",
                "Đơn hàng #" + order.getId() + " đã được tạo",
                NotificationType.ORDER_CREATED,
                order.getId(),
                order.getUser().getId(),
                ReferenceType.ORDER
        );

        notificationService.sendNotification(notificationRequest);

        // Build response
        OrderResponse orderResponse = OrderResponse.fromEntity(order);
        orderResponse.setItems(itemResponses);

        return orderResponse;
    }

    private OrderEntity buildOrderEntity(
            Long shopId,
            List<CartItemEntity> items,
            UserAddressEntity address,
            VoucherEntity voucher,
            CreateOrderRequest request) {
        ShopEntity shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));
        OrderEntity order = new OrderEntity();
        order.setUser(getCurrentUser());
        order.setShopId(shopId);
        order.setShopName(shop.getShopName());
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

    private boolean isAllowedSellerTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        return switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.CONFIRMED
                    || newStatus == OrderStatus.SHIPPING
                    || newStatus == OrderStatus.COMPLETED;
            case CONFIRMED -> newStatus == OrderStatus.SHIPPING
                    || newStatus == OrderStatus.COMPLETED;
            case SHIPPING -> newStatus == OrderStatus.COMPLETED;
            default -> false;
        };
    }

    private OrderStatusHistoryEntity addInitialStatusHistory(OrderEntity order,
                                                             OrderStatus from,
                                                             OrderStatus to,
                                                             UserRole userRole,
                                                             Long currentUserId) {
        OrderStatusHistoryEntity orderStatusHistory = new OrderStatusHistoryEntity();
        orderStatusHistory.setFromStatus(from);
        orderStatusHistory.setToStatus(to);
        orderStatusHistory.setChangedBy(userRole);
        orderStatusHistory.setChangedById(currentUserId);
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

    private List<OrderItemResponse> createOrderItems(
            OrderEntity order,
            List<CartItemEntity> items) {

        List<OrderItemEntity> orderItems = new ArrayList<>();
        List<ProductVariantEntity> updatedVariants = new ArrayList<>();
        List<OrderItemResponse> itemResponses = new ArrayList<>();

        for (CartItemEntity cartItem : items) {
            OrderItemEntity orderItem = createOrderItem(order, cartItem);
            orderItems.add(orderItem);

            // Update stock
            ProductVariantEntity variant = updateProductStock(cartItem);
            updatedVariants.add(variant);

            // Build response
            itemResponses.add(OrderItemResponse.fromEntity(orderItem));
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
