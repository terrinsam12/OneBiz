package com.clouderp.controller;

import com.clouderp.dto.ProductDto;
import com.clouderp.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public ResponseEntity<ProductDto.Response> createProduct(@Valid @RequestBody ProductDto.Create dto) {
        ProductDto.Response response = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<ProductDto.Response> getProducts(
            @RequestParam(value = "company_id", defaultValue = "1") Long companyId) {
        return productService.getProductsByCompany(companyId);
    }
}
