package com.prj.ecommerce.service;

import com.prj.ecommerce.dto.request.CreateProductRequest;
import com.prj.ecommerce.dto.response.CreateProductResponse;

public interface ProductService {
    CreateProductResponse createProduct(CreateProductRequest request);
}
