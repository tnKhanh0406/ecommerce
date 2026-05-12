package com.prj.ecommerce.api;

import com.prj.ecommerce.dto.request.voucher.VoucherRequest;
import com.prj.ecommerce.dto.response.voucher.VoucherResponse;
import com.prj.ecommerce.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/voucher")
public class VoucherApiController {
    private final VoucherService voucherService;

    @GetMapping("/{shopId}")
    public List<VoucherResponse> getAllByShopId(@PathVariable Long shopId) {
        return voucherService.getVoucherByShopId(shopId);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping
    public ResponseEntity<VoucherResponse> createVoucher(@Valid @RequestBody VoucherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(voucherService.createVoucher(request));
    }

    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/{voucherId}")
    public ResponseEntity<VoucherResponse> updateVoucher(@PathVariable Long voucherId, @Valid @RequestBody VoucherRequest request) {
        return ResponseEntity.ok(voucherService.updateVoucher(voucherId, request));
    }

    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/{voucherId}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long voucherId) {
        voucherService.deleteVoucher(voucherId);
        return ResponseEntity.noContent().build();
    }
}
