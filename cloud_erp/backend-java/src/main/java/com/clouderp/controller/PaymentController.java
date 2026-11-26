package com.clouderp.controller;

import com.clouderp.dto.PaymentDto;
import com.clouderp.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public ResponseEntity<PaymentDto.Response> recordPayment(@Valid @RequestBody PaymentDto.RecordRequest request) {
        PaymentDto.Response response = paymentService.recordPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<List<PaymentDto.Response>> getPayments(
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "saleId", required = false) Long saleId) {
        List<PaymentDto.Response> list = paymentService.getAllPayments(customerId, saleId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/receivables/summary")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<PaymentDto.ReceivablesSummary> getReceivablesSummary() {
        PaymentDto.ReceivablesSummary summary = paymentService.getReceivablesSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/receivables/invoices")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<List<PaymentDto.InvoiceReceivable>> getInvoiceReceivables(
            @RequestParam(value = "status", defaultValue = "ALL") String status,
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "search", required = false) String search) {
        List<PaymentDto.InvoiceReceivable> list = paymentService.getInvoiceReceivables(status, customerId, search);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/receivables/customers")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<List<PaymentDto.CustomerReceivable>> getCustomerReceivables(
            @RequestParam(value = "search", required = false) String search) {
        List<PaymentDto.CustomerReceivable> list = paymentService.getCustomerReceivables(search);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/sale/{saleId}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<List<PaymentDto.Response>> getPaymentsForSale(@PathVariable Long saleId) {
        List<PaymentDto.Response> list = paymentService.getPaymentsForSale(saleId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<List<PaymentDto.Response>> getPaymentsForCustomer(@PathVariable Long customerId) {
        List<PaymentDto.Response> list = paymentService.getPaymentsForCustomer(customerId);
        return ResponseEntity.ok(list);
    }
}
