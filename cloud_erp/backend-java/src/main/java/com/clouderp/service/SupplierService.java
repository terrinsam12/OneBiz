package com.clouderp.service;

import com.clouderp.config.SecurityUtils;
import com.clouderp.dto.PurchaseDto;
import com.clouderp.dto.SupplierDto;
import com.clouderp.exception.BadRequestException;
import com.clouderp.exception.ResourceNotFoundException;
import com.clouderp.model.Purchase;
import com.clouderp.model.Supplier;
import com.clouderp.repository.PurchaseRepository;
import com.clouderp.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final PurchaseRepository purchaseRepository;

    public SupplierService(SupplierRepository supplierRepository, PurchaseRepository purchaseRepository) {
        this.supplierRepository = supplierRepository;
        this.purchaseRepository = purchaseRepository;
    }

    @Transactional(readOnly = true)
    public List<SupplierDto.Response> getSuppliers(String search) {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        List<Supplier> suppliers = supplierRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);

        return suppliers.stream()
                .filter(s -> {
                    if (!StringUtils.hasText(search)) return true;
                    String q = search.trim().toLowerCase();
                    boolean matchName = s.getName() != null && s.getName().toLowerCase().contains(q);
                    boolean matchContact = s.getContactPerson() != null && s.getContactPerson().toLowerCase().contains(q);
                    boolean matchPhone = s.getPhone() != null && s.getPhone().toLowerCase().contains(q);
                    boolean matchEmail = s.getEmail() != null && s.getEmail().toLowerCase().contains(q);
                    boolean matchAddress = s.getAddress() != null && s.getAddress().toLowerCase().contains(q);
                    return matchName || matchContact || matchPhone || matchEmail || matchAddress;
                })
                .map(this::toResponseWithStats)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupplierDto.Response getSupplierById(Long id) {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        Supplier supplier = supplierRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        return toResponseWithStats(supplier);
    }

    public SupplierDto.Response createSupplier(SupplierDto.Create request) {
        SecurityUtils.requireAnyRole("COMPANY_ADMIN", "MANAGER");
        Long companyId = SecurityUtils.requireCurrentCompanyId();

        Supplier supplier = new Supplier(
                companyId,
                request.getName().trim(),
                request.getContactPerson() != null ? request.getContactPerson().trim() : "",
                request.getPhone() != null ? request.getPhone().trim() : "",
                request.getEmail() != null ? request.getEmail().trim() : "",
                request.getAddress() != null ? request.getAddress().trim() : ""
        );

        Supplier saved = supplierRepository.save(supplier);
        return toResponseWithStats(saved);
    }

    public SupplierDto.Response updateSupplier(Long id, SupplierDto.Update request) {
        SecurityUtils.requireAnyRole("COMPANY_ADMIN", "MANAGER");
        Long companyId = SecurityUtils.requireCurrentCompanyId();

        Supplier supplier = supplierRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        supplier.setName(request.getName().trim());
        supplier.setContactPerson(request.getContactPerson() != null ? request.getContactPerson().trim() : "");
        supplier.setPhone(request.getPhone() != null ? request.getPhone().trim() : "");
        supplier.setEmail(request.getEmail() != null ? request.getEmail().trim() : "");
        supplier.setAddress(request.getAddress() != null ? request.getAddress().trim() : "");

        Supplier updated = supplierRepository.save(supplier);
        return toResponseWithStats(updated);
    }

    public void deleteSupplier(Long id) {
        SecurityUtils.requireAnyRole("COMPANY_ADMIN", "MANAGER");
        Long companyId = SecurityUtils.requireCurrentCompanyId();

        Supplier supplier = supplierRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        long purchaseCount = purchaseRepository.countByCompanyIdAndSupplierId(companyId, id);
        if (purchaseCount > 0) {
            throw new BadRequestException("Cannot delete supplier with existing purchase records (" + purchaseCount + " orders found).");
        }

        supplierRepository.delete(supplier);
    }

    private SupplierDto.Response toResponseWithStats(Supplier supplier) {
        SupplierDto.Response resp = SupplierDto.Response.fromSupplier(supplier);
        List<Purchase> purchases = purchaseRepository.findByCompanyIdAndSupplierIdOrderByCreatedAtDesc(
                supplier.getCompanyId(), supplier.getId());

        resp.setPurchaseCount((long) purchases.size());
        double totalSpend = purchases.stream()
                .filter(p -> "RECEIVED".equalsIgnoreCase(p.getStatus()))
                .mapToDouble(Purchase::getTotalAmount)
                .sum();
        resp.setTotalSpend(totalSpend);
        return resp;
    }
}
