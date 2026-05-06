package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.voucher.CreateVoucherRequest;
import com.prj.ecommerce.dto.response.voucher.VoucherResponse;

import java.util.List;

public interface VoucherService {
    List<VoucherResponse> getVoucherByShopId(Long shopId);
    VoucherResponse createVoucher(CreateVoucherRequest request);
    VoucherResponse updateVoucher(Long voucherId, CreateVoucherRequest request);
    void deleteVoucher(Long voucherId);
}
