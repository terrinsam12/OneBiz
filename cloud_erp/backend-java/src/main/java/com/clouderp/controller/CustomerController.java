package com.clouderp.controller;

import com.clouderp.dto.CustomerDto;
import com.clouderp.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerDto.Response> createCustomer(@Valid @RequestBody CustomerDto.Create dto) {
        CustomerDto.Response response = customerService.createCustomer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{companyId}")
    public CustomerDto.ListResponse getCustomers(@PathVariable Long companyId) {
        return customerService.getCustomers(companyId);
    }

    @GetMapping
    public CustomerDto.ListResponse getCustomersQuery(
            @RequestParam(value = "company_id", defaultValue = "1") Long companyId) {
        return customerService.getCustomers(companyId);
    }
}
