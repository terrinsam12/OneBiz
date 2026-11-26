package com.clouderp.controller;

import com.clouderp.dto.InventoryDto;
import com.clouderp.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/stock-in")
    public InventoryDto.StockOperationResponse stockIn(@Valid @RequestBody InventoryDto.Request dto) {
        return inventoryService.stockIn(dto);
    }

    @PostMapping("/stock-out")
    public InventoryDto.StockOperationResponse stockOut(@Valid @RequestBody InventoryDto.Request dto) {
        return inventoryService.stockOut(dto);
    }

    @GetMapping("/{companyId}")
    public InventoryDto.Response getInventory(@PathVariable Long companyId) {
        return inventoryService.getInventory(companyId);
    }
}
