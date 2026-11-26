package com.clouderp.service;

import com.clouderp.config.SecurityUtils;
import com.clouderp.dto.ProductDto;
import com.clouderp.model.Product;
import com.clouderp.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CompanyService companyService;

    public ProductService(ProductRepository productRepository, CompanyService companyService) {
        this.productRepository = productRepository;
        this.companyService = companyService;
    }

    public ProductDto.Response createProduct(ProductDto.Create dto) {
        // Enforce company ID from authenticated user, never trust client input
        Long companyId = SecurityUtils.getCurrentCompanyId();
        companyService.getCompanyById(companyId);

        Product product = new Product(
                companyId,
                dto.getName().trim(),
                dto.getPrice(),
                dto.getQuantity(),
                dto.getCostPrice(),
                dto.getReorderLevel()
        );

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductDto.Response> getProductsByCompany(Long requestedCompanyId) {
        // Enforce company ID from authenticated user
        Long companyId = SecurityUtils.getCurrentCompanyId();
        companyService.getCompanyById(companyId);

        return productRepository.findByCompanyId(companyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Product getProductByIdAndCompany(Long productId, Long companyId) {
        return productRepository.findByIdAndCompanyId(productId, companyId)
                .orElse(null);
    }

    private ProductDto.Response toResponse(Product p) {
        return new ProductDto.Response(
                p.getId(),
                p.getCompanyId(),
                p.getName(),
                p.getPrice(),
                p.getQuantity(),
                p.getCostPrice(),
                p.getReorderLevel()
        );
    }
}
