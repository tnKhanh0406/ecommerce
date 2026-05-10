package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.voucher.VoucherRequest;
import com.prj.ecommerce.dto.response.voucher.VoucherResponse;

import java.util.List;

public interface VoucherService {
    List<VoucherResponse> getVoucherByShopId(Long shopId);
    VoucherResponse createVoucher(VoucherRequest request);
    VoucherResponse updateVoucher(Long voucherId, VoucherRequest request);
    void deleteVoucher(Long voucherId);
}
