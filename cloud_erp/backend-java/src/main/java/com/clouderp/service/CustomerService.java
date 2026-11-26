package com.clouderp.service;

import com.clouderp.config.SecurityUtils;
import com.clouderp.dto.CustomerDto;
import com.clouderp.model.Company;
import com.clouderp.model.Customer;
import com.clouderp.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CompanyService companyService;

    public CustomerService(CustomerRepository customerRepository, CompanyService companyService) {
        this.customerRepository = customerRepository;
        this.companyService = companyService;
    }

    public CustomerDto.Response createCustomer(CustomerDto.Create dto) {
        // Enforce company ID from authenticated user
        Long companyId = SecurityUtils.getCurrentCompanyId();
        companyService.getCompanyById(companyId);

        Customer customer = new Customer(
                companyId,
                dto.getName().trim(),
                dto.getPhone() != null ? dto.getPhone().trim() : "",
                dto.getEmail() != null ? dto.getEmail().trim() : "",
                dto.getAddress() != null ? dto.getAddress().trim() : ""
        );

        Customer saved = customerRepository.save(customer);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CustomerDto.ListResponse getCustomers(Long requestedCompanyId) {
        // Enforce company ID from authenticated user
        Long companyId = SecurityUtils.getCurrentCompanyId();
        Company company = companyService.getCompanyById(companyId);

        List<Customer> list = customerRepository.findByCompanyId(companyId);
        List<CustomerDto.Response> responses = list.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new CustomerDto.ListResponse(company.getId(), company.getName(), responses);
    }

    private CustomerDto.Response toResponse(Customer c) {
        return new CustomerDto.Response(
                c.getId(),
                c.getCompanyId(),
                c.getName(),
                c.getPhone(),
                c.getEmail(),
                c.getAddress()
        );
    }
}
