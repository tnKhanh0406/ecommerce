package com.prj.ecommerce.entity;

import com.prj.ecommerce.common.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "shops")
public class ShopEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String shopName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column()
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private Double rating = 0.0;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = Status.ACTIVE;
        }
    }
}
