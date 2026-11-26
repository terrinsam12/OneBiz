package com.clouderp.controller;

import com.clouderp.dto.ExpenseDto;
import com.clouderp.exception.BadRequestException;
import com.clouderp.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private static final String UPLOAD_DIR = "./data/receipts";

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
        File uploadFolder = new File(UPLOAD_DIR);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }
    }

    @GetMapping
    public List<ExpenseDto.Response> getExpenses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return expenseService.getExpenses(search, category, startDate, endDate);
    }

    @GetMapping("/{id}")
    public ExpenseDto.Response getExpenseById(@PathVariable Long id) {
        return expenseService.getExpenseById(id);
    }

    @GetMapping("/summary")
    public ExpenseDto.SummaryResponse getSummary() {
        return expenseService.getSummary();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public ResponseEntity<ExpenseDto.Response> createExpense(@Valid @RequestBody ExpenseDto.Create request) {
        ExpenseDto.Response created = expenseService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public ExpenseDto.Response updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseDto.Update request) {
        return expenseService.updateExpense(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'MANAGER')")
    public ResponseEntity<ExpenseDto.UploadResponse> uploadReceipt(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded receipt file cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        String safeName = originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_") : "receipt";
        String uniqueFileName = UUID.randomUUID().toString().substring(0, 8) + "_" + safeName;

        try {
            Path targetLocation = Paths.get(UPLOAD_DIR).resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/expenses/receipts/" + uniqueFileName;
            return ResponseEntity.ok(new ExpenseDto.UploadResponse(fileUrl, originalFilename));
        } catch (IOException e) {
            throw new BadRequestException("Could not store receipt file: " + e.getMessage());
        }
    }

    @GetMapping("/receipts/{fileName:.+}")
    public ResponseEntity<Resource> getReceipt(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
