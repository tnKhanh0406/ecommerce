package com.prj.ecommerce.dto.response;

import com.prj.ecommerce.entity.OrderStatusHistoryEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryResponse {
    private String fromStatus;
    private String toStatus;
    private String changedBy;
    private Long changedById;
    private String note;
    private LocalDateTime createdAt;

    public static OrderHistoryResponse fromEntity(OrderStatusHistoryEntity e) {
        OrderHistoryResponse r = new OrderHistoryResponse();
        r.setFromStatus(e.getFromStatus() != null ? e.getFromStatus().name() : null);
        r.setToStatus(e.getToStatus().name());
        r.setChangedBy(e.getChangedBy() != null ? e.getChangedBy().name() : null);
        r.setChangedById(e.getChangedById());
        r.setNote(e.getNote() != null ? e.getNote() : null);
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}
