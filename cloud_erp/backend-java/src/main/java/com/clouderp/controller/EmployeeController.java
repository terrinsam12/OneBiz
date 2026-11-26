package com.clouderp.controller;

import com.clouderp.dto.EmployeeDto;
import com.clouderp.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
@PreAuthorize("hasRole('COMPANY_ADMIN')")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<EmployeeDto.Response> getEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active) {
        return employeeService.getEmployees(search, role, active);
    }

    @GetMapping("/{id}")
    public EmployeeDto.Response getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id);
    }

    @PostMapping
    public ResponseEntity<EmployeeDto.Response> createEmployee(@Valid @RequestBody EmployeeDto.CreateRequest request) {
        EmployeeDto.Response created = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public EmployeeDto.Response updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDto.UpdateRequest request) {
        return employeeService.updateEmployee(id, request);
    }

    @PatchMapping("/{id}/status")
    public EmployeeDto.Response updateStatus(
            @PathVariable Long id,
            @RequestBody EmployeeDto.StatusUpdateRequest request) {
        Boolean active = request.getIsActive() != null ? request.getIsActive() : true;
        return employeeService.updateEmployeeStatus(id, active);
    }
}
