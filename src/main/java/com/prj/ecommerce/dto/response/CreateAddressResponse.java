package com.prj.ecommerce.dto.response;

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
public class CreateAddressResponse {
    private Long addressId;
    private String receiverName;
    private String receiverPhone;
    private String address;
    private Integer isDefault;
    private LocalDateTime createdAt;
    private Long userId;

    public static CreateAddressResponse fromEntity(UserAddressEntity userAddressEntity) {
        CreateAddressResponse createAddressResponse = new CreateAddressResponse();
        createAddressResponse.setAddressId(userAddressEntity.getId());
        createAddressResponse.setReceiverName(userAddressEntity.getReceiverName());
        createAddressResponse.setReceiverPhone(userAddressEntity.getReceiverPhone());
        createAddressResponse.setAddress(userAddressEntity.getAddress());
        createAddressResponse.setIsDefault(userAddressEntity.getIsDefault());
        createAddressResponse.setCreatedAt(userAddressEntity.getCreatedAt());
        createAddressResponse.setUserId(userAddressEntity.getUser().getId());
        return createAddressResponse;
    }
}
