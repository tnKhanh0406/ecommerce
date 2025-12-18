package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.CreateVoucherRequest;
import com.prj.ecommerce.dto.response.VoucherResponse;

public interface VoucherService {
    VoucherResponse createVoucher(CreateVoucherRequest request);
}
