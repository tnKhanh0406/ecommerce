package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.dto.request.AddCartItemRequest;
import com.prj.ecommerce.dto.response.AddCartItemResponse;
import com.prj.ecommerce.entity.CartEntity;
import com.prj.ecommerce.entity.CartItemEntity;
import com.prj.ecommerce.entity.ProductVariantEntity;
import com.prj.ecommerce.entity.UserEntity;
import com.prj.ecommerce.repository.CartItemRepository;
import com.prj.ecommerce.repository.CartRepository;
import com.prj.ecommerce.repository.ProductVariantRepository;
import com.prj.ecommerce.repository.UserRepository;
import com.prj.ecommerce.service.CartService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;

    private UserEntity getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public AddCartItemResponse addCartItem(AddCartItemRequest addCartItemRequest) {
        CartEntity cartEntity = cartRepository.findByUser_Id(getCurrentUser().getId());
        if (cartEntity == null) {
            cartEntity = new CartEntity();
            cartEntity.setUser(getCurrentUser());
            cartRepository.save(cartEntity);
        }
        CartItemEntity cartItemEntity = new CartItemEntity();
        cartItemEntity.setCart(cartEntity);
        cartItemEntity.setQuantity(addCartItemRequest.getQuantity());

        ProductVariantEntity entity = productVariantRepository.findById(addCartItemRequest.getItem().getId())
                .orElseThrow(() -> new EntityNotFoundException("Product variant not found"));

        cartItemEntity.setProductVariant(entity);
        cartItemRepository.save(cartItemEntity);
        return AddCartItemResponse.fromEntity(cartItemEntity);
    }
}
