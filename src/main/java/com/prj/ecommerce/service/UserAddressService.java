package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.CreateAddressRequest;
import com.prj.ecommerce.dto.response.CreateAddressResponse;

import java.util.List;

public interface UserAddressService {
    CreateAddressResponse createAddress(CreateAddressRequest request);
    CreateAddressResponse updateAddress(Long addressId, CreateAddressRequest request);
    void deleteAddress(Long addressId);
    List<CreateAddressResponse> getAllAddresses();
    void setDefaultAddress(Long addressId);
}
