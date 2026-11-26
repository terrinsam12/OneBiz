package com.clouderp.service;

import com.clouderp.config.SecurityUtils;
import com.clouderp.dto.SaleDto;
import com.clouderp.exception.BadRequestException;
import com.clouderp.exception.ResourceNotFoundException;
import com.clouderp.model.Customer;
import com.clouderp.model.Product;
import com.clouderp.model.Sale;
import com.clouderp.repository.CustomerRepository;
import com.clouderp.repository.ProductRepository;
import com.clouderp.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CompanyService companyService;
    private final NotificationService notificationService;

    public SaleService(SaleRepository saleRepository,
                       ProductRepository productRepository,
                       CustomerRepository customerRepository,
                       CompanyService companyService,
                       NotificationService notificationService) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.companyService = companyService;
        this.notificationService = notificationService;
    }

    public SaleDto.CreateResponse createSale(SaleDto.Create dto) {
        // Enforce company ID from authenticated user
        Long companyId = SecurityUtils.getCurrentCompanyId();
        companyService.getCompanyById(companyId);

        Product product = productRepository.findByIdAndCompanyId(dto.getProductId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for this company"));

        if (product.getQuantity() < dto.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Current stock: " + product.getQuantity());
        }

        String customerName = null;
        if (dto.getCustomerId() != null) {
            Customer customer = customerRepository.findByIdAndCompanyId(dto.getCustomerId(), companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found for this company"));
            customerName = customer.getName();
        }

        // Deduct inventory
        product.setQuantity(product.getQuantity() - dto.getQuantity());
        productRepository.save(product);

        double totalAmount = product.getPrice() * dto.getQuantity();

        Sale sale = new Sale(
                companyId,
                product.getId(),
                dto.getCustomerId(),
                dto.getQuantity(),
                product.getPrice(),
                totalAmount
        );
        sale.setPaidAmount(0.0);
        sale.setBalanceAmount(totalAmount);
        sale.setPaymentStatus("UNPAID");
        sale.setDueDate(java.time.LocalDate.now(java.time.ZoneOffset.UTC).plusDays(15));

        Sale saved = saleRepository.save(sale);
        if (saved.getInvoiceNumber() == null) {
            saved.setInvoiceNumber("INV-" + (1000 + saved.getId()));
            saved = saleRepository.save(saved);
        }

        // Trigger notification and check stock levels
        notificationService.onSaleCreated(saved, product.getName());
        notificationService.checkProductStockAlert(product);

        String createdAtStr = saved.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String dueDateStr = saved.getDueDate() != null ? saved.getDueDate().toString() : null;

        SaleDto.Item item = new SaleDto.Item(
                saved.getId(),
                saved.getCompanyId(),
                saved.getProductId(),
                product.getName(),
                saved.getCustomerId(),
                customerName,
                saved.getQuantity(),
                saved.getUnitPrice(),
                saved.getTotalAmount(),
                createdAtStr,
                saved.getInvoiceNumber(),
                saved.getPaidAmount(),
                saved.getBalanceAmount(),
                saved.getPaymentStatus(),
                dueDateStr
        );

        return new SaleDto.CreateResponse("Sale recorded successfully", item, product.getQuantity());
    }

    @Transactional(readOnly = true)
    public List<SaleDto.Item> getSales(Long requestedCompanyId) {
        // Enforce company ID from authenticated user
        Long companyId = SecurityUtils.getCurrentCompanyId();
        companyService.getCompanyById(companyId);

        List<Sale> sales = saleRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
        List<Product> products = productRepository.findByCompanyId(companyId);
        List<Customer> customers = customerRepository.findByCompanyId(companyId);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<Long, Customer> customerMap = customers.stream()
                .collect(Collectors.toMap(Customer::getId, Function.identity()));

        return sales.stream().map(sale -> {
            Product prod = productMap.get(sale.getProductId());
            Customer cust = sale.getCustomerId() != null ? customerMap.get(sale.getCustomerId()) : null;

            String prodName = prod != null ? prod.getName() : "Unknown Product";
            String custName = cust != null ? cust.getName() : null;
            String dateStr = sale.getCreatedAt() != null
                    ? sale.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    : null;
            String invNum = sale.getInvoiceNumber() != null ? sale.getInvoiceNumber() : "INV-" + sale.getId();
            String dueDateStr = sale.getDueDate() != null ? sale.getDueDate().toString() : null;

            return new SaleDto.Item(
                    sale.getId(),
                    sale.getCompanyId(),
                    sale.getProductId(),
                    prodName,
                    sale.getCustomerId(),
                    custName,
                    sale.getQuantity(),
                    sale.getUnitPrice(),
                    sale.getTotalAmount(),
                    dateStr,
                    invNum,
                    sale.getPaidAmount(),
                    sale.getBalanceAmount(),
                    sale.getPaymentStatus(),
                    dueDateStr
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SaleDto.SummaryResponse getSalesSummary(Long requestedCompanyId) {
        // Enforce company ID from authenticated user
        Long companyId = SecurityUtils.getCurrentCompanyId();
        companyService.getCompanyById(companyId);

        List<Sale> sales = saleRepository.findByCompanyId(companyId);

        double totalSales = 0.0;
        int totalUnits = 0;
        int totalTransactions = sales.size();

        for (Sale s : sales) {
            totalSales += s.getTotalAmount() != null ? s.getTotalAmount() : 0.0;
            totalUnits += s.getQuantity() != null ? s.getQuantity() : 0;
        }

        return new SaleDto.SummaryResponse(
                companyId,
                Math.round(totalSales * 100.0) / 100.0,
                totalUnits,
                totalTransactions
        );
    }
}
