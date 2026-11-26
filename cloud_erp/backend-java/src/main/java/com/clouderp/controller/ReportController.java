package com.clouderp.controller;

import com.clouderp.dto.ReportDto;
import com.clouderp.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ReportDto.SalesReport> getSalesReport(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "preset", required = false) String preset) {
        return ResponseEntity.ok(reportService.getSalesReport(startDate, endDate, preset));
    }

    @GetMapping("/purchases")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ReportDto.PurchaseReport> getPurchaseReport(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "preset", required = false) String preset) {
        return ResponseEntity.ok(reportService.getPurchaseReport(startDate, endDate, preset));
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ReportDto.InventoryReport> getInventoryReport() {
        return ResponseEntity.ok(reportService.getInventoryReport());
    }

    @GetMapping("/expenses")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ReportDto.ExpenseReport> getExpenseReport(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "preset", required = false) String preset) {
        return ResponseEntity.ok(reportService.getExpenseReport(startDate, endDate, preset));
    }

    @GetMapping("/profit-loss")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public ResponseEntity<ReportDto.ProfitLossReport> getProfitLossReport(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "preset", required = false) String preset) {
        return ResponseEntity.ok(reportService.getProfitLossReport(startDate, endDate, preset));
    }

    @GetMapping("/receivables")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ReportDto.ReceivablesReport> getReceivablesReport(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "preset", required = false) String preset) {
        return ResponseEntity.ok(reportService.getReceivablesReport(startDate, endDate, preset));
    }

    @GetMapping("/{reportType}/export")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportReport(
            @PathVariable("reportType") String reportType,
            @RequestParam(value = "format", defaultValue = "pdf") String format,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "preset", required = false) String preset) {

        byte[] content = reportService.exportReport(reportType, format, startDate, endDate, preset);

        String ext = "excel".equalsIgnoreCase(format) || "xlsx".equalsIgnoreCase(format) || "xls".equalsIgnoreCase(format) ? "xls" : "pdf";
        MediaType mediaType = "xls".equalsIgnoreCase(ext)
                ? MediaType.parseMediaType("application/vnd.ms-excel; charset=UTF-8")
                : MediaType.APPLICATION_PDF;

        String filename = String.format("%s_report_%s.%s", reportType.toLowerCase(), LocalDate.now(), ext);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(content);
    }
}
