package com.clouderp.service;

import com.clouderp.config.SecurityUtils;
import com.clouderp.dto.PaymentDto;
import com.clouderp.exception.BadRequestException;
import com.clouderp.exception.ResourceNotFoundException;
import com.clouderp.model.Customer;
import com.clouderp.model.Payment;
import com.clouderp.model.Product;
import com.clouderp.model.Sale;
import com.clouderp.repository.CustomerRepository;
import com.clouderp.repository.PaymentRepository;
import com.clouderp.repository.ProductRepository;
import com.clouderp.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public PaymentService(PaymentRepository paymentRepository,
                          SaleRepository saleRepository,
                          CustomerRepository customerRepository,
                          ProductRepository productRepository,
                          NotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.saleRepository = saleRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    public PaymentDto.Response recordPayment(PaymentDto.RecordRequest request) {
        SecurityUtils.requireAnyRole("COMPANY_ADMIN", "MANAGER");
        Long companyId = SecurityUtils.requireCurrentCompanyId();

        Sale sale = saleRepository.findByIdAndCompanyId(request.getSaleId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice/Sale not found for this company"));

        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new BadRequestException("Payment amount must be greater than zero");
        }

        double currentBalance = sale.getBalanceAmount() != null ? sale.getBalanceAmount() : sale.getTotalAmount();
        if (request.getAmount() > currentBalance + 0.001) {
            throw new BadRequestException("Payment amount (₹" + round(request.getAmount()) +
                    ") cannot exceed outstanding invoice balance of ₹" + round(currentBalance));
        }

        String method = StringUtils.hasText(request.getPaymentMethod())
                ? request.getPaymentMethod().trim()
                : "Cash";

        List<String> validMethods = Arrays.asList("Cash", "UPI", "Bank Transfer", "Card");
        if (!validMethods.contains(method)) {
            throw new BadRequestException("Invalid payment method. Allowed methods: Cash, UPI, Bank Transfer, Card");
        }

        LocalDate payDate = request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now(ZoneOffset.UTC);

        // Update Sale Invoice payment status and balance
        double currentPaid = sale.getPaidAmount() != null ? sale.getPaidAmount() : 0.0;
        double newPaid = round(currentPaid + request.getAmount());
        double newBalance = round(Math.max(0.0, sale.getTotalAmount() - newPaid));

        sale.setPaidAmount(newPaid);
        sale.setBalanceAmount(newBalance);

        if (newBalance <= 0.001) {
            sale.setPaymentStatus("PAID");
        } else {
            sale.setPaymentStatus("PARTIALLY_PAID");
        }
        saleRepository.save(sale);

        // Generate Payment Number
        long count = paymentRepository.countByCompanyId(companyId);
        String paymentNumber = "PAY-" + (count + 1001);

        Payment payment = new Payment(
                companyId,
                sale.getId(),
                sale.getCustomerId(),
                paymentNumber,
                round(request.getAmount()),
                payDate,
                method,
                request.getReferenceNumber() != null ? request.getReferenceNumber().trim() : null,
                request.getNotes() != null ? request.getNotes().trim() : null
        );

        Payment savedPayment = paymentRepository.save(payment);

        // Fetch customer name
        String customerName = "Walk-in Customer";
        if (sale.getCustomerId() != null) {
            Customer cust = customerRepository.findByIdAndCompanyId(sale.getCustomerId(), companyId).orElse(null);
            if (cust != null) {
                customerName = cust.getName();
            }
        }

        // Trigger payment received notification
        notificationService.onPaymentReceived(savedPayment, sale, customerName);

        return mapToResponse(savedPayment, sale, customerName);
    }

    @Transactional(readOnly = true)
    public PaymentDto.ReceivablesSummary getReceivablesSummary() {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        List<Sale> sales = saleRepository.findByCompanyId(companyId);

        double totalInvoiced = 0.0;
        double totalPaid = 0.0;
        double totalReceivables = 0.0;
        double totalOverdue = 0.0;

        int unpaidCount = 0;
        int partiallyPaidCount = 0;
        int paidCount = 0;
        int overdueCount = 0;

        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        for (Sale s : sales) {
            double total = s.getTotalAmount() != null ? s.getTotalAmount() : 0.0;
            double paid = s.getPaidAmount() != null ? s.getPaidAmount() : 0.0;
            double balance = s.getBalanceAmount() != null ? s.getBalanceAmount() : Math.max(0.0, total - paid);

            totalInvoiced += total;
            totalPaid += paid;
            if (balance > 0.001) {
                totalReceivables += balance;
            }

            String status = s.getPaymentStatus() != null ? s.getPaymentStatus() : "UNPAID";
            if ("PAID".equalsIgnoreCase(status)) {
                paidCount++;
            } else if ("PARTIALLY_PAID".equalsIgnoreCase(status)) {
                partiallyPaidCount++;
            } else {
                unpaidCount++;
            }

            // Overdue check
            if (balance > 0.001 && s.getDueDate() != null && s.getDueDate().isBefore(today)) {
                totalOverdue += balance;
                overdueCount++;
            }
        }

        double collectionRatePct = totalInvoiced > 0 ? (totalPaid / totalInvoiced) * 100.0 : 0.0;

        PaymentDto.ReceivablesSummary summary = new PaymentDto.ReceivablesSummary();
        summary.setTotalInvoiced(round(totalInvoiced));
        summary.setTotalPaid(round(totalPaid));
        summary.setTotalReceivables(round(totalReceivables));
        summary.setTotalOverdue(round(totalOverdue));
        summary.setTotalInvoicesCount(sales.size());
        summary.setPaidInvoicesCount(paidCount);
        summary.setPartiallyPaidInvoicesCount(partiallyPaidCount);
        summary.setUnpaidInvoicesCount(unpaidCount);
        summary.setOverdueInvoicesCount(overdueCount);
        summary.setCollectionRatePct(round(collectionRatePct));

        return summary;
    }

    @Transactional(readOnly = true)
    public List<PaymentDto.InvoiceReceivable> getInvoiceReceivables(String status, Long customerId, String search) {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        List<Sale> sales = saleRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
        List<Product> products = productRepository.findByCompanyId(companyId);
        List<Customer> customers = customerRepository.findByCompanyId(companyId);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));
        Map<Long, Customer> customerMap = customers.stream()
                .collect(Collectors.toMap(Customer::getId, Function.identity(), (a, b) -> a));

        // Count payments per sale
        List<Payment> payments = paymentRepository.findByCompanyId(companyId);
        Map<Long, Long> paymentsCountMap = payments.stream()
                .collect(Collectors.groupingBy(Payment::getSaleId, Collectors.counting()));

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        String searchLower = StringUtils.hasText(search) ? search.trim().toLowerCase() : null;

        List<PaymentDto.InvoiceReceivable> result = new ArrayList<>();

        for (Sale s : sales) {
            Product prod = productMap.get(s.getProductId());
            Customer cust = s.getCustomerId() != null ? customerMap.get(s.getCustomerId()) : null;

            String prodName = prod != null ? prod.getName() : "Product #" + s.getProductId();
            String custName = cust != null ? cust.getName() : "Walk-in Customer";
            String invNum = StringUtils.hasText(s.getInvoiceNumber()) ? s.getInvoiceNumber() : "INV-" + s.getId();

            double total = s.getTotalAmount() != null ? s.getTotalAmount() : 0.0;
            double paid = s.getPaidAmount() != null ? s.getPaidAmount() : 0.0;
            double balance = s.getBalanceAmount() != null ? s.getBalanceAmount() : Math.max(0.0, total - paid);
            String pStatus = s.getPaymentStatus() != null ? s.getPaymentStatus() : (balance <= 0.001 ? "PAID" : (paid > 0 ? "PARTIALLY_PAID" : "UNPAID"));

            boolean isOverdue = balance > 0.001 && s.getDueDate() != null && s.getDueDate().isBefore(today);
            Long daysOverdue = isOverdue ? ChronoUnit.DAYS.between(s.getDueDate(), today) : null;

            // Filter by customerId
            if (customerId != null && !Objects.equals(s.getCustomerId(), customerId)) {
                continue;
            }

            // Filter by status
            if (StringUtils.hasText(status) && !"ALL".equalsIgnoreCase(status)) {
                if ("OVERDUE".equalsIgnoreCase(status)) {
                    if (!isOverdue) continue;
                } else if (!pStatus.equalsIgnoreCase(status)) {
                    continue;
                }
            }

            // Filter by search
            if (searchLower != null) {
                boolean matchInv = invNum.toLowerCase().contains(searchLower);
                boolean matchCust = custName.toLowerCase().contains(searchLower);
                boolean matchProd = prodName.toLowerCase().contains(searchLower);
                if (!matchInv && !matchCust && !matchProd) {
                    continue;
                }
            }

            PaymentDto.InvoiceReceivable item = new PaymentDto.InvoiceReceivable();
            item.setId(s.getId());
            item.setInvoiceNumber(invNum);
            item.setCompanyId(s.getCompanyId());
            item.setProductId(s.getProductId());
            item.setProductName(prodName);
            item.setCustomerId(s.getCustomerId());
            item.setCustomerName(custName);
            item.setQuantity(s.getQuantity());
            item.setUnitPrice(s.getUnitPrice());
            item.setTotalAmount(round(total));
            item.setPaidAmount(round(paid));
            item.setBalanceAmount(round(balance));
            item.setPaymentStatus(pStatus);
            item.setDueDate(s.getDueDate() != null ? s.getDueDate().toString() : null);
            item.setIsOverdue(isOverdue);
            item.setDaysOverdue(daysOverdue);
            item.setCreatedAt(s.getCreatedAt() != null ? s.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null);
            item.setPaymentsCount(paymentsCountMap.getOrDefault(s.getId(), 0L).intValue());

            result.add(item);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<PaymentDto.CustomerReceivable> getCustomerReceivables(String search) {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        List<Customer> customers = customerRepository.findByCompanyId(companyId);
        List<Sale> sales = saleRepository.findByCompanyId(companyId);
        List<Payment> payments = paymentRepository.findByCompanyId(companyId);

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        String searchLower = StringUtils.hasText(search) ? search.trim().toLowerCase() : null;

        Map<Long, List<Sale>> salesByCustomer = sales.stream()
                .filter(s -> s.getCustomerId() != null)
                .collect(Collectors.groupingBy(Sale::getCustomerId));

        Map<Long, List<Payment>> paymentsByCustomer = payments.stream()
                .filter(p -> p.getCustomerId() != null)
                .collect(Collectors.groupingBy(Payment::getCustomerId));

        List<PaymentDto.CustomerReceivable> list = new ArrayList<>();

        for (Customer c : customers) {
            if (searchLower != null) {
                boolean matchName = c.getName() != null && c.getName().toLowerCase().contains(searchLower);
                boolean matchPhone = c.getPhone() != null && c.getPhone().toLowerCase().contains(searchLower);
                boolean matchEmail = c.getEmail() != null && c.getEmail().toLowerCase().contains(searchLower);
                if (!matchName && !matchPhone && !matchEmail) {
                    continue;
                }
            }

            List<Sale> custSales = salesByCustomer.getOrDefault(c.getId(), Collections.emptyList());
            List<Payment> custPayments = paymentsByCustomer.getOrDefault(c.getId(), Collections.emptyList());

            double totalInvoiced = 0.0;
            double totalPaid = 0.0;
            double outstanding = 0.0;
            double overdue = 0.0;
            int unpaidCount = 0;

            for (Sale s : custSales) {
                double total = s.getTotalAmount() != null ? s.getTotalAmount() : 0.0;
                double paid = s.getPaidAmount() != null ? s.getPaidAmount() : 0.0;
                double balance = s.getBalanceAmount() != null ? s.getBalanceAmount() : Math.max(0.0, total - paid);

                totalInvoiced += total;
                totalPaid += paid;
                if (balance > 0.001) {
                    outstanding += balance;
                    unpaidCount++;
                    if (s.getDueDate() != null && s.getDueDate().isBefore(today)) {
                        overdue += balance;
                    }
                }
            }

            String lastPayDate = custPayments.stream()
                    .map(Payment::getPaymentDate)
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .map(LocalDate::toString)
                    .orElse(null);

            PaymentDto.CustomerReceivable dto = new PaymentDto.CustomerReceivable();
            dto.setCustomerId(c.getId());
            dto.setCustomerName(c.getName());
            dto.setPhone(c.getPhone());
            dto.setEmail(c.getEmail());
            dto.setTotalInvoiced(round(totalInvoiced));
            dto.setTotalPaid(round(totalPaid));
            dto.setOutstandingBalance(round(outstanding));
            dto.setOverdueBalance(round(overdue));
            dto.setInvoicesCount(custSales.size());
            dto.setUnpaidInvoicesCount(unpaidCount);
            dto.setLastPaymentDate(lastPayDate);

            list.add(dto);
        }

        list.sort((a, b) -> Double.compare(b.getOutstandingBalance(), a.getOutstandingBalance()));
        return list;
    }

    @Transactional(readOnly = true)
    public List<PaymentDto.Response> getPaymentsForSale(Long saleId) {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        Sale sale = saleRepository.findByIdAndCompanyId(saleId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        String custName = "Walk-in Customer";
        if (sale.getCustomerId() != null) {
            Customer cust = customerRepository.findByIdAndCompanyId(sale.getCustomerId(), companyId).orElse(null);
            if (cust != null) custName = cust.getName();
        }

        final String finalCustName = custName;
        return paymentRepository.findByCompanyIdAndSaleIdOrderByPaymentDateDesc(companyId, saleId).stream()
                .map(p -> mapToResponse(p, sale, finalCustName))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentDto.Response> getPaymentsForCustomer(Long customerId) {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        Customer cust = customerRepository.findByIdAndCompanyId(customerId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        List<Payment> payments = paymentRepository.findByCompanyIdAndCustomerIdOrderByPaymentDateDesc(companyId, customerId);
        List<Sale> sales = saleRepository.findByCompanyId(companyId);
        Map<Long, Sale> saleMap = sales.stream().collect(Collectors.toMap(Sale::getId, Function.identity(), (a, b) -> a));

        return payments.stream()
                .map(p -> mapToResponse(p, saleMap.get(p.getSaleId()), cust.getName()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentDto.Response> getAllPayments(Long customerId, Long saleId) {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        List<Payment> list;

        if (saleId != null) {
            list = paymentRepository.findByCompanyIdAndSaleIdOrderByPaymentDateDesc(companyId, saleId);
        } else if (customerId != null) {
            list = paymentRepository.findByCompanyIdAndCustomerIdOrderByPaymentDateDesc(companyId, customerId);
        } else {
            list = paymentRepository.findByCompanyIdOrderByPaymentDateDescCreatedAtDesc(companyId);
        }

        List<Sale> sales = saleRepository.findByCompanyId(companyId);
        List<Customer> customers = customerRepository.findByCompanyId(companyId);

        Map<Long, Sale> saleMap = sales.stream().collect(Collectors.toMap(Sale::getId, Function.identity(), (a, b) -> a));
        Map<Long, Customer> custMap = customers.stream().collect(Collectors.toMap(Customer::getId, Function.identity(), (a, b) -> a));

        return list.stream().map(p -> {
            Sale s = saleMap.get(p.getSaleId());
            Customer c = p.getCustomerId() != null ? custMap.get(p.getCustomerId()) : null;
            String cName = c != null ? c.getName() : "Walk-in Customer";
            return mapToResponse(p, s, cName);
        }).collect(Collectors.toList());
    }

    private PaymentDto.Response mapToResponse(Payment p, Sale s, String customerName) {
        PaymentDto.Response resp = new PaymentDto.Response();
        resp.setId(p.getId());
        resp.setCompanyId(p.getCompanyId());
        resp.setSaleId(p.getSaleId());
        resp.setInvoiceNumber(s != null && StringUtils.hasText(s.getInvoiceNumber()) ? s.getInvoiceNumber() : "INV-" + p.getSaleId());
        resp.setCustomerId(p.getCustomerId());
        resp.setCustomerName(customerName);
        resp.setPaymentNumber(p.getPaymentNumber());
        resp.setAmount(p.getAmount());
        resp.setPaymentDate(p.getPaymentDate());
        resp.setPaymentMethod(p.getPaymentMethod());
        resp.setReferenceNumber(p.getReferenceNumber());
        resp.setNotes(p.getNotes());
        resp.setCreatedAt(p.getCreatedAt() != null ? p.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null);
        if (s != null) {
            resp.setInvoiceTotal(s.getTotalAmount());
            resp.setInvoiceBalance(s.getBalanceAmount());
            resp.setInvoicePaymentStatus(s.getPaymentStatus());
        }
        return resp;
    }

    private static double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
