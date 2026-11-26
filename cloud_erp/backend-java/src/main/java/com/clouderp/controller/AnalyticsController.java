package com.clouderp.controller;

import com.clouderp.dto.AnalyticsDto;
import com.clouderp.service.AnalyticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    public AnalyticsDto.OverviewResponse getOverview(
            @RequestParam(value = "company_id", defaultValue = "1") Long companyId) {
        return analyticsService.getOverview(companyId);
    }
}
