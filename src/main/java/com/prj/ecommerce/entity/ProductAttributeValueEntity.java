package com.prj.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table (name = "product_attribute_values")
@Entity
@Getter @Setter
public class ProductAttributeValueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id")
    private ProductAttributeEntity productAttribute;
}
