package com.prj.ecommerce.dto.response.user;

import com.prj.ecommerce.entity.UserAddressEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private Long addressId;
    private String receiverName;
    private String receiverPhone;
    private String address;
    private Integer isDefault;
    private LocalDateTime createdAt;
    private Long userId;

    public static AddressResponse fromEntity(UserAddressEntity userAddressEntity) {
        AddressResponse addressResponse = new AddressResponse();
        addressResponse.setAddressId(userAddressEntity.getId());
        addressResponse.setReceiverName(userAddressEntity.getReceiverName());
        addressResponse.setReceiverPhone(userAddressEntity.getReceiverPhone());
        addressResponse.setAddress(userAddressEntity.getAddress());
        addressResponse.setIsDefault(userAddressEntity.getIsDefault());
        addressResponse.setCreatedAt(userAddressEntity.getCreatedAt());
        addressResponse.setUserId(userAddressEntity.getUser().getId());
        return addressResponse;
    }
}
