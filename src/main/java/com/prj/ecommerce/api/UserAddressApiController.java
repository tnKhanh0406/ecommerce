package com.prj.ecommerce.api;

import com.prj.ecommerce.dto.request.user.CreateAddressRequest;
import com.prj.ecommerce.dto.response.user.CreateAddressResponse;
import com.prj.ecommerce.service.UserAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class UserAddressApiController {
    private final UserAddressService userAddressService;

    @GetMapping
    public List<CreateAddressResponse> getAllAddresses() {
        return userAddressService.getAllAddresses();
    }

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
