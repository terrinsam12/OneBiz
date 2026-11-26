package com.clouderp.controller;

import com.clouderp.dto.CompanyDto;
import com.clouderp.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<CompanyDto.Response> createCompany(@Valid @RequestBody CompanyDto.Create dto) {
        CompanyDto.Response response = companyService.createCompany(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<CompanyDto.Response> getCompanies() {
        return companyService.getAllCompanies();
    }
}
