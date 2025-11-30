package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.dto.request.CreateAddressRequest;
import com.prj.ecommerce.dto.response.CreateAddressResponse;
import com.prj.ecommerce.entity.UserAddressEntity;
import com.prj.ecommerce.entity.UserEntity;
import com.prj.ecommerce.repository.UserAddressRepository;
import com.prj.ecommerce.repository.UserRepository;
import com.prj.ecommerce.service.UserAddressService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    private UserEntity getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    public CreateAddressResponse createAddress(CreateAddressRequest request) {
        UserAddressEntity userAddress = new UserAddressEntity();
        UserAddressEntity address = userAddressRepository.findByUser_IdAndIsDefault(getCurrentUser().getId(), 1).orElse(null);
        if (address == null) {
            userAddress.setIsDefault(1);
        } else {
            if (request.getIsDefault() != null && request.getIsDefault().equals(1)) {
                address.setIsDefault(0);
            }
        }
        userAddress.setAddress(request.getAddress());
        userAddress.setUser(getCurrentUser());
        userAddress.setIsDefault(request.getIsDefault());
        userAddress.setReceiverName(request.getReceiverName());
        userAddress.setReceiverPhone(request.getReceiverPhone());
        return CreateAddressResponse.fromEntity(userAddressRepository.save(userAddress));
    }

    @Override
    public CreateAddressResponse updateAddress(Long addressId, CreateAddressRequest request) {
        UserAddressEntity userAddress = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("UserAddress not found"));
        if (!getCurrentUser().getId().equals(userAddress.getUser().getId())) {
            throw new AccessDeniedException("You do not have permission to update this address");
        }
        UserAddressEntity address = userAddressRepository.findByUser_IdAndIsDefault(getCurrentUser().getId(), 1).orElse(null);
        if (address != null && request.getIsDefault().equals(1)) {
            address.setIsDefault(0);
        }
        userAddress.setAddress(request.getAddress());
        userAddress.setReceiverPhone(request.getReceiverPhone());
        userAddress.setReceiverName(request.getReceiverName());
        userAddress.setIsDefault(request.getIsDefault());
        return CreateAddressResponse.fromEntity(userAddressRepository.save(userAddress));
    }

    @Override
    public void deleteAddress(Long addressId) {
        UserAddressEntity userAddress = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("UserAddress not found"));
        if (!getCurrentUser().getId().equals(userAddress.getUser().getId())) {
            throw new AccessDeniedException("You do not have permission to update this address");
        }
        userAddressRepository.delete(userAddress);
    }

    @Override
    public List<CreateAddressResponse> getAllAddresses() {
        return userAddressRepository.findAllByUser_Id(getCurrentUser().getId())
                .stream()
                .map(CreateAddressResponse::fromEntity)
                .toList();
    }
}
