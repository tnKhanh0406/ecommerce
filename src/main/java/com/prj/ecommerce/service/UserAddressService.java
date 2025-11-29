package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.CreateAddressRequest;
import com.prj.ecommerce.dto.response.CreateAddressResponse;

public interface UserAddressService {
    CreateAddressResponse createAddress(CreateAddressRequest request);
    CreateAddressResponse updateAddress(Long addressId, CreateAddressRequest request);
    void deleteAddress(Long addressId);
}
