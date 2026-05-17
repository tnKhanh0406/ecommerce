package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.dto.request.voucher.VoucherRequest;
import com.prj.ecommerce.dto.response.voucher.VoucherResponse;
import com.prj.ecommerce.entity.ShopEntity;
import com.prj.ecommerce.entity.VoucherEntity;
import com.prj.ecommerce.model.UserPrincipal;
import com.prj.ecommerce.repository.ShopRepository;
import com.prj.ecommerce.repository.VoucherRepository;
import com.prj.ecommerce.service.VoucherService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final ShopRepository shopRepository;

    private Long getCurrentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal())
                .getId();
    }

    private boolean checkShopOwnership(Long voucherId) {
        return voucherRepository.existsByIdAndUser(voucherId, getCurrentUserId());
    }

    @Override
    public List<VoucherResponse> getVoucherByShopId(Long shopId) {
        List<VoucherEntity> voucherEntities = voucherRepository.findAllByShopId(shopId);
        List<VoucherResponse> voucherResponses = new ArrayList<>();
        if (voucherEntities != null) {
            voucherResponses = voucherEntities.stream()
                    .map(VoucherResponse::fromEntity)
                    .toList();
        }
        return voucherResponses;
    }

    @Override
    public VoucherResponse createVoucher(VoucherRequest request) {
        ShopEntity shop = shopRepository.findByUser_Id(getCurrentUserId())
                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));

        VoucherEntity voucher = new VoucherEntity();

        voucher.setCode(request.getCode());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setShop(shop);
        voucher.setEndAt(request.getEndAt());
        voucher.setStartAt(request.getStartAt());
        voucher.setMaxDiscount(request.getMaxDiscount());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setUsageLimit(request.getUsageLimit());

        return VoucherResponse.fromEntity(voucherRepository.save(voucher));
    }

    @Override
    public VoucherResponse updateVoucher(Long voucherId, VoucherRequest request) {
        VoucherEntity voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found"));
        if (!checkShopOwnership(voucherId)) {
            throw new AccessDeniedException("You do not have permission to update this voucher");
        }
        voucher.setCode(request.getCode());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setEndAt(request.getEndAt());
        voucher.setStartAt(request.getStartAt());
        voucher.setMaxDiscount(request.getMaxDiscount());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setUsageLimit(request.getUsageLimit());
        return VoucherResponse.fromEntity(voucherRepository.save(voucher));
    }

    @Override
    public void deleteVoucher(Long voucherId) {
        VoucherEntity voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found"));
        if (!checkShopOwnership(voucherId)) {
            throw new AccessDeniedException("You do not have permission to delete this voucher");
        }
        voucherRepository.delete(voucher);
    }
}
