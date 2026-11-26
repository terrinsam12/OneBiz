package com.clouderp.controller;

import com.clouderp.dto.ForecastDto;
import com.clouderp.service.ForecastService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
public class ForecastController {

    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @GetMapping("/forecast")
    public ForecastDto.Response getForecast(
            @RequestParam(value = "company_id", defaultValue = "1") Long companyId) {
        return forecastService.getForecast(companyId);
    }
}
