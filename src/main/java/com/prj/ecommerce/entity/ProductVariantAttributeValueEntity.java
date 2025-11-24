package com.prj.ecommerce.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "variant_attribute_values")
public class ProductVariantAttributeValueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String displayName;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariantEntity variant;

    @ManyToOne
    @JoinColumn(name = "attribute_value_id")
    private ProductAttributeValueEntity attributeValue;
}
