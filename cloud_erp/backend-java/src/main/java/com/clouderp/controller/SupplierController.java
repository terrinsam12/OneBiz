package com.clouderp.controller;

import com.clouderp.dto.PurchaseDto;
import com.clouderp.dto.SupplierDto;
import com.clouderp.service.PurchaseService;
import com.clouderp.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    private final SupplierService supplierService;
    private final PurchaseService purchaseService;

    public SupplierController(SupplierService supplierService, PurchaseService purchaseService) {
        this.supplierService = supplierService;
        this.purchaseService = purchaseService;
    }

    @GetMapping
    public List<SupplierDto.Response> getSuppliers(@RequestParam(required = false) String search) {
        return supplierService.getSuppliers(search);
    }

    @GetMapping("/{id}")
    public SupplierDto.Response getSupplierById(@PathVariable Long id) {
        return supplierService.getSupplierById(id);
    }

    @GetMapping("/{id}/purchases")
    public List<PurchaseDto.Response> getSupplierPurchases(@PathVariable Long id) {
        return purchaseService.getPurchases(id, null, null);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public ResponseEntity<SupplierDto.Response> createSupplier(@Valid @RequestBody SupplierDto.Create request) {
        SupplierDto.Response created = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public SupplierDto.Response updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierDto.Update request) {
        return supplierService.updateSupplier(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}
