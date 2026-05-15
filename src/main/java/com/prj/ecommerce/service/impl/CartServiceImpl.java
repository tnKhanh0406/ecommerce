package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.dto.request.cart.AddCartItemRequest;
import com.prj.ecommerce.dto.request.cart.UpdateCartItemRequest;
import com.prj.ecommerce.dto.response.cart.CartItemSummaryResponse;
import com.prj.ecommerce.dto.response.cart.HeaderCartItemResponse;
import com.prj.ecommerce.entity.CartEntity;
import com.prj.ecommerce.entity.CartItemEntity;
import com.prj.ecommerce.entity.ProductVariantEntity;
import com.prj.ecommerce.entity.UserEntity;
import com.prj.ecommerce.exception.BadRequestException;
import com.prj.ecommerce.repository.*;
import com.prj.ecommerce.service.CartService;
import com.prj.ecommerce.utils.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductVariantAttributeValueRepository productVariantAttributeValueRepository;

    private UserEntity getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private boolean checkUserOwnsCartItem(Long cartItemId) {
        return !cartItemRepository.existsByIdAndUser(cartItemId, SecurityUtil.getCurrentUserId());
    }

    @Override
    public List<HeaderCartItemResponse> getTop5CartItems() {
        return cartItemRepository.getTop5HeaderCartItems(SecurityUtil.getCurrentUserId(), PageRequest.of(0, 5));
    }

    @Override
    public long getCartItemCount() {
        return cartItemRepository.countByCartUserId(SecurityUtil.getCurrentUserId());
    }

    @Override
    public List<CartItemSummaryResponse> getCartItems() {
        Long userId = SecurityUtil.getCurrentUserId();

        List<CartItemSummaryResponse> items = cartItemRepository.findCartItemSummaries(userId);

        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        // lấy variant ids
        List<Long> variantIds = items.stream()
                .map(CartItemSummaryResponse::getVariantId)
                .toList();

        // query attributes
        List<Object[]> rows = productVariantAttributeValueRepository.findVariantAttributes(variantIds);

        Map<Long, List<String>> attrMap = new HashMap<>();

        for (Object[] row : rows) {
            Long variantId = (Long) row[0];
            String name = (String) row[1];
            String value = (String) row[2];

            attrMap.computeIfAbsent(variantId, k -> new ArrayList<>()).add(name + ": " + value);
        }

        // set attributes
        items.forEach(item -> {
            item.setAttributes(attrMap.getOrDefault(item.getVariantId(),Collections.emptyList()));
        });

        return items;
    }


    @Override
    @Transactional
    public CartItemSummaryResponse addCartItem(AddCartItemRequest addCartItemRequest) {
        UserEntity currentUser = getCurrentUser();
        if (addCartItemRequest.getQuantity() < 1) {
            throw new IllegalArgumentException("Quantity must be >= 1");
        }

        // 1. Lấy cart hoặc tạo mới
        CartEntity cartEntity = cartRepository.findByUser_Id(currentUser.getId());
        if (cartEntity == null) {
            cartEntity = new CartEntity();
            cartEntity.setUser(currentUser);
            cartRepository.saveAndFlush(cartEntity);
        }

        // 2. Kiểm tra item trong cart
        CartItemEntity cartItemEntity = cartItemRepository.findByCart_IdAndProductVariant_Id(
                        cartEntity.getId(),
                        addCartItemRequest.getVariantId());

        ProductVariantEntity variant = productVariantRepository.findById(addCartItemRequest.getVariantId())
                        .orElseThrow(() -> new EntityNotFoundException("Product variant not found"));

        if (productVariantRepository.existsByIdAndProductShopUserId(variant.getId(), currentUser.getId())) {
            throw new BadRequestException("Seller can not buy their own product");
        }
        if (cartItemEntity == null) {
            // Tạo mới
            cartItemEntity = new CartItemEntity();
            cartItemEntity.setCart(cartEntity);
            cartItemEntity.setProductVariant(variant);
            cartItemEntity.setQuantity(addCartItemRequest.getQuantity());
            cartItemEntity.setShopId(variant.getProduct().getShop().getId());

            cartEntity.getCartItems().add(cartItemEntity);
        } else {
            int newQty = cartItemEntity.getQuantity() + addCartItemRequest.getQuantity();
            cartItemEntity.setQuantity(newQty);
        }

        // 3. Kiểm tra tồn kho
        if (cartItemEntity.getQuantity() > variant.getStock()) {
            throw new IllegalArgumentException("Out of stock");
        }

        cartItemRepository.save(cartItemEntity);
        return mapToCartItemSummaryResponse(cartItemEntity, addCartItemRequest.getVariantId());
    }

    @Override
    public CartItemSummaryResponse updateCartItem(UpdateCartItemRequest updateCartItemRequest) {
        CartItemEntity cartItemEntity = cartItemRepository.findById(updateCartItemRequest.getCartItemId())
                .orElseThrow(() -> new EntityNotFoundException("Cart Item not found"));
        if (checkUserOwnsCartItem(updateCartItemRequest.getCartItemId())) {
            throw new AccessDeniedException("This cart item does not belong to user");
        }

        ProductVariantEntity variant = cartItemEntity.getProductVariant();

        // Kiểm tra tồn kho
        if (updateCartItemRequest.getQuantity() > variant.getStock()) {
            throw new IllegalArgumentException("Out of stock");
        }

        if (updateCartItemRequest.getQuantity() < 1) {
            throw new IllegalArgumentException("Quantity must be >= 1");
        }

        cartItemEntity.setQuantity(updateCartItemRequest.getQuantity());
        cartItemRepository.save(cartItemEntity);
        return mapToCartItemSummaryResponse(cartItemEntity, variant.getId());
    }

    @Override
    public void deleteCartItem(Long cartItemId) {
        CartItemEntity cartItemEntity = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart Item not found"));
        if (checkUserOwnsCartItem(cartItemId)) {
            throw new AccessDeniedException("This cart item does not belong to user");
        }
        cartItemRepository.delete(cartItemEntity);
    }

    private CartItemSummaryResponse mapToCartItemSummaryResponse(CartItemEntity cartItemEntity, Long variantId) {
        List<Object[]> rows = productVariantAttributeValueRepository.findVariantAttributesByVariantId(variantId);
        List<String> attributes = rows.stream()
            .map(row -> row[0] + ": " + row[1])
            .toList();
        CartItemSummaryResponse response = cartItemRepository.findCartItemSummaryById(cartItemEntity.getId())
            .orElseThrow(() -> new EntityNotFoundException("Cart item summary not found"));
        response.setAttributes(attributes);
        return response;
    }
}
