package com.prj.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "voucher_usages",
        uniqueConstraints = @UniqueConstraint(columnNames = {"voucher_id", "user_id"})
)
@Getter
@Setter
public class VoucherUsageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "voucher_id")
    private VoucherEntity voucher;

    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(nullable = false, name = "order_id")
    private OrderEntity order;

    private LocalDateTime usedAt;

    @PrePersist
    public void prePersist() {
        if (usedAt == null) {
            usedAt = LocalDateTime.now();
        }
    }
}

