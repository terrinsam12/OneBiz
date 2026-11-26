package com.clouderp.service;

import com.clouderp.config.SecurityUtils;
import com.clouderp.dto.InventoryDto;
import com.clouderp.exception.BadRequestException;
import com.clouderp.exception.ResourceNotFoundException;
import com.clouderp.model.Company;
import com.clouderp.model.Product;
import com.clouderp.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryService {

    private final ProductRepository productRepository;
    private final CompanyService companyService;
    private final NotificationService notificationService;

    public InventoryService(ProductRepository productRepository,
                            CompanyService companyService,
                            NotificationService notificationService) {
        this.productRepository = productRepository;
        this.companyService = companyService;
        this.notificationService = notificationService;
    }

    public InventoryDto.StockOperationResponse stockIn(InventoryDto.Request dto) {
        // Enforce company ID from authenticated user
        Long companyId = SecurityUtils.getCurrentCompanyId();

        Product product = productRepository.findByIdAndCompanyId(dto.getProductId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for this company"));

        product.setQuantity(product.getQuantity() + dto.getQuantity());
        Product saved = productRepository.save(product);

        notificationService.checkProductStockAlert(saved);

        return InventoryDto.StockOperationResponse.forStockIn(
                product.getCompanyId(),
                product.getId(),
                product.getName(),
                dto.getQuantity(),
                product.getQuantity(),
                product.getReorderLevel()
        );
    }

    public InventoryDto.StockOperationResponse stockOut(InventoryDto.Request dto) {
        // Enforce company ID from authenticated user
        Long companyId = SecurityUtils.getCurrentCompanyId();

        Product product = productRepository.findByIdAndCompanyId(dto.getProductId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for this company"));

        if (product.getQuantity() < dto.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Current stock: " + product.getQuantity());
        }

        product.setQuantity(product.getQuantity() - dto.getQuantity());
        Product saved = productRepository.save(product);

        notificationService.checkProductStockAlert(saved);

        return InventoryDto.StockOperationResponse.forStockOut(
                product.getCompanyId(),
                product.getId(),
                product.getName(),
                dto.getQuantity(),
                product.getQuantity(),
                product.getReorderLevel()
        );
    }

    @Transactional(readOnly = true)
    public InventoryDto.Response getInventory(Long requestedCompanyId) {
        // Enforce company ID from authenticated user
        Long companyId = SecurityUtils.getCurrentCompanyId();
        Company company = companyService.getCompanyById(companyId);

        List<Product> products = productRepository.findByCompanyId(companyId);
        List<InventoryDto.Item> items = products.stream()
                .map(p -> {
                    String status = p.getQuantity() <= p.getReorderLevel() ? "LOW STOCK" : "AVAILABLE";
                    return new InventoryDto.Item(
                            p.getId(),
                            p.getName(),
                            p.getPrice(),
                            p.getQuantity(),
                            p.getReorderLevel(),
                            status
                    );
                })
                .collect(Collectors.toList());

        return new InventoryDto.Response(company.getId(), company.getName(), items);
    }
}
