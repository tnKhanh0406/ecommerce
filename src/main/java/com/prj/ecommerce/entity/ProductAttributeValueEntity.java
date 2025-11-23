package com.prj.ecommerce.entity;

import jakarta.persistence.*;

@Table (name = "product_attribute_values")
@Entity
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
