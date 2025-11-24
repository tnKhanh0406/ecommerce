package com.prj.ecommerce.service.impl;

import com.prj.ecommerce.dto.request.CreateProductRequest;
import com.prj.ecommerce.dto.response.CreateProductResponse;
import com.prj.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    @Override
    public CreateProductResponse createProduct(CreateProductRequest request) {

        return null;
    }
}
