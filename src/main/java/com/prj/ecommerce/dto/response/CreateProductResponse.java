package com.prj.ecommerce.dto.response;

import com.prj.ecommerce.entity.ProductEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductResponse {
    private Long id;
    private String name;
    private String description;

    public static CreateProductResponse fromEntity(ProductEntity p) {
        CreateProductResponse r = new CreateProductResponse();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setDescription(p.getStatus().name());
        return r;
    }
}
