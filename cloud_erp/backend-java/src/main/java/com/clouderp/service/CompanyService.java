package com.clouderp.service;

import com.clouderp.config.SecurityUtils;
import com.clouderp.dto.CompanyDto;
import com.clouderp.exception.ResourceNotFoundException;
import com.clouderp.model.Company;
import com.clouderp.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public CompanyDto.Response createCompany(CompanyDto.Create dto) {
        Company company = new Company(dto.getName().trim(), dto.getIndustry() != null ? dto.getIndustry().trim() : "");
        Company saved = companyRepository.save(company);
        return new CompanyDto.Response(saved.getId(), saved.getName(), saved.getIndustry());
    }

    @Transactional(readOnly = true)
    public List<CompanyDto.Response> getAllCompanies() {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        if (companyId != null) {
            try {
                Company c = getCompanyById(companyId);
                return List.of(new CompanyDto.Response(c.getId(), c.getName(), c.getIndustry()));
            } catch (ResourceNotFoundException ignored) {
            }
        }
        return companyRepository.findAll().stream()
                .map(c -> new CompanyDto.Response(c.getId(), c.getName(), c.getIndustry()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
    }
}
