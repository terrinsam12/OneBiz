package com.clouderp.controller;

import com.clouderp.dto.FinanceDto;
import com.clouderp.service.FinanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/finance")
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/overview")
    public ResponseEntity<FinanceDto.SummaryResponse> getFinancialSummary(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        FinanceDto.SummaryResponse summary = financeService.getFinancialSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }
}
