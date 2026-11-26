package com.clouderp.controller;

import com.clouderp.dto.PurchaseDto;
import com.clouderp.service.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping
    public List<PurchaseDto.Response> getPurchases(
            @RequestParam(value = "supplier_id", required = false) Long supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return purchaseService.getPurchases(supplierId, status, search);
    }

    @GetMapping("/{id}")
    public PurchaseDto.Response getPurchaseById(@PathVariable Long id) {
        return purchaseService.getPurchaseById(id);
    }

    @GetMapping("/summary")
    public PurchaseDto.SummaryResponse getSummary() {
        return purchaseService.getSummary();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public ResponseEntity<PurchaseDto.Response> createPurchase(@Valid @RequestBody PurchaseDto.Create request) {
        PurchaseDto.Response created = purchaseService.createPurchase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public PurchaseDto.Response updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseDto.StatusUpdate request) {
        return purchaseService.updatePurchaseStatus(id, request.getStatus());
    }
}
