package com.prj.ecommerce.controller;

import com.prj.ecommerce.dto.request.CreateAddressRequest;
import com.prj.ecommerce.dto.response.CreateAddressResponse;
import com.prj.ecommerce.service.UserAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class UserAddressController {
    private final UserAddressService userAddressService;

    @PostMapping
    public ResponseEntity<CreateAddressResponse> createAddress(@Valid @RequestBody CreateAddressRequest request) {
        CreateAddressResponse createAddressResponse = userAddressService.createAddress(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createAddressResponse);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<CreateAddressResponse> updateAddress(@PathVariable Long addressId,
                                                               @Valid @RequestBody CreateAddressRequest request) {
        CreateAddressResponse createAddressResponse = userAddressService.updateAddress(addressId, request);
        return ResponseEntity.ok(createAddressResponse);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long addressId) {
        userAddressService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }
}
