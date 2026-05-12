package com.prj.ecommerce.api;

import com.prj.ecommerce.dto.request.user.AddressRequest;
import com.prj.ecommerce.dto.response.user.AddressResponse;
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
    public List<AddressResponse> getAllAddresses() {
        return userAddressService.getAllAddresses();
    }

    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(@Valid @RequestBody AddressRequest request) {
        AddressResponse addressResponse = userAddressService.createAddress(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(addressResponse);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable Long addressId,
                                                         @Valid @RequestBody AddressRequest request) {
        AddressResponse addressResponse = userAddressService.updateAddress(addressId, request);
        return ResponseEntity.ok(addressResponse);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long addressId) {
        userAddressService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }
}
