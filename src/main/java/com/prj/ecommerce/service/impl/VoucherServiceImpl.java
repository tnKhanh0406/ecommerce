package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.dto.request.CreateVoucherRequest;
import com.prj.ecommerce.dto.response.VoucherResponse;
import com.prj.ecommerce.entity.ShopEntity;
import com.prj.ecommerce.entity.UserEntity;
import com.prj.ecommerce.entity.VoucherEntity;
import com.prj.ecommerce.model.UserPrincipal;
import com.prj.ecommerce.repository.ShopRepository;
import com.prj.ecommerce.repository.UserRepository;
import com.prj.ecommerce.repository.VoucherRepository;
import com.prj.ecommerce.service.VoucherService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    private UserEntity getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private Long getCurrentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal())
                .getUserEntity().getId();
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
    public VoucherResponse createVoucher(CreateVoucherRequest request) {
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
    public VoucherResponse updateVoucher(Long voucherId, CreateVoucherRequest request) {
        VoucherEntity voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new EntityNotFoundException("Voucher not found"));
        if (!voucher.getShop().getUser().getId().equals(getCurrentUserId())) {
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
        if (!voucher.getShop().getUser().getId().equals(getCurrentUserId())) {
            throw new AccessDeniedException("You do not have permission to delete this voucher");
        }
        voucherRepository.delete(voucher);
    }
}
