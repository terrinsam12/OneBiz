package com.clouderp.controller;

import com.clouderp.dto.SaleDto;
import com.clouderp.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    public ResponseEntity<SaleDto.CreateResponse> createSale(@Valid @RequestBody SaleDto.Create dto) {
        SaleDto.CreateResponse response = saleService.createSale(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{companyId}")
    public List<SaleDto.Item> getSales(@PathVariable Long companyId) {
        return saleService.getSales(companyId);
    }

    @GetMapping("/{companyId}/summary")
    public SaleDto.SummaryResponse getSalesSummary(@PathVariable Long companyId) {
        return saleService.getSalesSummary(companyId);
    }
}
