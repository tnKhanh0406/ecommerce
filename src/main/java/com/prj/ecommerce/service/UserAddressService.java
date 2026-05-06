package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.user.AddressRequest;
import com.prj.ecommerce.dto.response.user.AddressResponse;

import java.util.List;

public interface UserAddressService {
    AddressResponse createAddress(AddressRequest request);
    AddressResponse updateAddress(Long addressId, AddressRequest request);
    void deleteAddress(Long addressId);
    List<AddressResponse> getAllAddresses();
    void setDefaultAddress(Long addressId);
}
