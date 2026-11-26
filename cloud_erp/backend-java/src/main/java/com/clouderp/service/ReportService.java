package com.clouderp.service;

import com.clouderp.config.SecurityUtils;
import com.clouderp.dto.ReportDto;
import com.clouderp.exception.BadRequestException;
import com.clouderp.model.*;
import com.clouderp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final CompanyService companyService;
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final ExpenseRepository expenseRepository;
    private final CustomerRepository customerRepository;
    private final ReportExportService reportExportService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_LABEL_FMT = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    public ReportService(CompanyService companyService,
                         SaleRepository saleRepository,
                         ProductRepository productRepository,
                         PurchaseRepository purchaseRepository,
                         SupplierRepository supplierRepository,
                         ExpenseRepository expenseRepository,
                         CustomerRepository customerRepository,
                         ReportExportService reportExportService) {
        this.companyService = companyService;
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.purchaseRepository = purchaseRepository;
        this.supplierRepository = supplierRepository;
        this.expenseRepository = expenseRepository;
        this.customerRepository = customerRepository;
        this.reportExportService = reportExportService;
    }

    // =========================================================================
    // DATE FILTER HELPER
    // =========================================================================

    public static class DateRangeBounds {
        public LocalDate startDate;
        public LocalDate endDate;
        public OffsetDateTime startDateTime;
        public OffsetDateTime endDateTime;
        public String preset;
        public String periodLabel;
    }

    private DateRangeBounds resolveDateRange(String startDateStr, String endDateStr, String preset) {
        DateRangeBounds bounds = new DateRangeBounds();
        LocalDate now = LocalDate.now(ZoneOffset.UTC);

        if (StringUtils.hasText(preset)) {
            String p = preset.trim().toLowerCase();
            bounds.preset = p;
            switch (p) {
                case "this_month":
                    bounds.startDate = now.withDayOfMonth(1);
                    bounds.endDate = now;
                    bounds.periodLabel = "This Month (" + bounds.startDate.format(DATE_FMT) + " to " + bounds.endDate.format(DATE_FMT) + ")";
                    break;
                case "last_month":
                    YearMonth lastMonth = YearMonth.from(now.minusMonths(1));
                    bounds.startDate = lastMonth.atDay(1);
                    bounds.endDate = lastMonth.atEndOfMonth();
                    bounds.periodLabel = "Last Month (" + bounds.startDate.format(DATE_FMT) + " to " + bounds.endDate.format(DATE_FMT) + ")";
                    break;
                case "this_quarter":
                    int currentMonth = now.getMonthValue();
                    int startMonth = ((currentMonth - 1) / 3) * 3 + 1;
                    bounds.startDate = LocalDate.of(now.getYear(), startMonth, 1);
                    bounds.endDate = now;
                    bounds.periodLabel = "This Quarter (" + bounds.startDate.format(DATE_FMT) + " to " + bounds.endDate.format(DATE_FMT) + ")";
                    break;
                case "this_year":
                    bounds.startDate = LocalDate.of(now.getYear(), 1, 1);
                    bounds.endDate = now;
                    bounds.periodLabel = "This Year (" + bounds.startDate.format(DATE_FMT) + " to " + bounds.endDate.format(DATE_FMT) + ")";
                    break;
                case "all_time":
                    bounds.startDate = null;
                    bounds.endDate = null;
                    bounds.periodLabel = "All Time History";
                    break;
                default:
                    // Treat as custom if custom dates are passed
                    break;
            }
        }

        // Custom bounds if explicitly passed or preset is custom
        if (bounds.startDate == null && bounds.endDate == null && (StringUtils.hasText(startDateStr) || StringUtils.hasText(endDateStr))) {
            if (StringUtils.hasText(startDateStr)) {
                try {
                    bounds.startDate = LocalDate.parse(startDateStr.trim());
                } catch (Exception e) {
                    throw new BadRequestException("Invalid startDate format. Expected YYYY-MM-DD");
                }
            }
            if (StringUtils.hasText(endDateStr)) {
                try {
                    bounds.endDate = LocalDate.parse(endDateStr.trim());
                } catch (Exception e) {
                    throw new BadRequestException("Invalid endDate format. Expected YYYY-MM-DD");
                }
            }

            if (bounds.startDate != null && bounds.endDate != null && bounds.startDate.isAfter(bounds.endDate)) {
                throw new BadRequestException("startDate cannot be after endDate");
            }

            bounds.preset = "custom";
            if (bounds.startDate != null && bounds.endDate != null) {
                bounds.periodLabel = bounds.startDate.format(DATE_FMT) + " to " + bounds.endDate.format(DATE_FMT);
            } else if (bounds.startDate != null) {
                bounds.periodLabel = "From " + bounds.startDate.format(DATE_FMT);
            } else {
                bounds.periodLabel = "Up to " + bounds.endDate.format(DATE_FMT);
            }
        }

        if (bounds.periodLabel == null) {
            bounds.periodLabel = "All Time";
            bounds.preset = "all_time";
        }

        if (bounds.startDate != null) {
            bounds.startDateTime = bounds.startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        }
        if (bounds.endDate != null) {
            bounds.endDateTime = bounds.endDate.atTime(23, 59, 59, 999999999).atOffset(ZoneOffset.UTC);
        }

        return bounds;
    }

    private ReportDto.FilterInfo toFilterInfo(DateRangeBounds bounds) {
        return new ReportDto.FilterInfo(
                bounds.startDate != null ? bounds.startDate.format(DATE_FMT) : null,
                bounds.endDate != null ? bounds.endDate.format(DATE_FMT) : null,
                bounds.preset,
                bounds.periodLabel
        );
    }

    // =========================================================================
    // 1. SALES REPORT
    // =========================================================================

    public ReportDto.SalesReport getSalesReport(String startDateStr, String endDateStr, String preset) {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        Company company = companyService.getCompanyById(companyId);
        DateRangeBounds bounds = resolveDateRange(startDateStr, endDateStr, preset);

        List<Sale> allSales = saleRepository.findByCompanyId(companyId);
        List<Product> products = productRepository.findByCompanyId(companyId);
        List<Customer> customers = customerRepository.findByCompanyId(companyId);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));
        Map<Long, Customer> customerMap = customers.stream()
                .collect(Collectors.toMap(Customer::getId, Function.identity(), (a, b) -> a));

        List<Sale> filtered = allSales.stream().filter(sale -> {
            if (sale.getCreatedAt() == null) return false;
            if (bounds.startDateTime != null && sale.getCreatedAt().isBefore(bounds.startDateTime)) return false;
            if (bounds.endDateTime != null && sale.getCreatedAt().isAfter(bounds.endDateTime)) return false;
            return true;
        }).sorted(Comparator.comparing(Sale::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        ReportDto.SalesReport report = new ReportDto.SalesReport();
        report.setCompanyName(company.getName());
        report.setFilter(toFilterInfo(bounds));

        double totalRevenue = 0.0;
        int totalUnitsSold = 0;
        Map<Long, int[]> prodUnitsMap = new HashMap<>(); // [units, revenue*100]
        List<ReportDto.SaleTransactionItem> txList = new ArrayList<>();

        for (Sale s : filtered) {
            double rev = s.getTotalAmount() != null ? s.getTotalAmount() : 0.0;
            int qty = s.getQuantity() != null ? s.getQuantity() : 0;
            totalRevenue += rev;
            totalUnitsSold += qty;

            Product prod = productMap.get(s.getProductId());
            Customer cust = s.getCustomerId() != null ? customerMap.get(s.getCustomerId()) : null;

            prodUnitsMap.putIfAbsent(s.getProductId(), new int[]{0, 0});
            int[] agg = prodUnitsMap.get(s.getProductId());
            agg[0] += qty;
            agg[1] += (int) Math.round(rev * 100);

            ReportDto.SaleTransactionItem item = new ReportDto.SaleTransactionItem();
            item.setId(s.getId());
            item.setInvoiceNumber(s.getInvoiceNumber() != null ? s.getInvoiceNumber() : "INV-" + s.getId());
            item.setSaleDate(s.getCreatedAt() != null ? s.getCreatedAt().format(DATE_FMT) : "");
            item.setCustomerName(cust != null ? cust.getName() : "Direct Customer");
            item.setProductName(prod != null ? prod.getName() : "Product #" + s.getProductId());
            item.setQuantity(qty);
            item.setUnitPrice(s.getUnitPrice() != null ? s.getUnitPrice() : 0.0);
            item.setTotalAmount(rev);
            item.setPaymentStatus(s.getPaymentStatus() != null ? s.getPaymentStatus() : "PAID");
            item.setPaidAmount(s.getPaidAmount() != null ? s.getPaidAmount() : rev);
            item.setBalanceAmount(s.getBalanceAmount() != null ? s.getBalanceAmount() : 0.0);
            txList.add(item);
        }

        report.setTotalRevenue(roundTwo(totalRevenue));
        report.setTotalTransactions(filtered.size());
        report.setTotalUnitsSold(totalUnitsSold);
        report.setAverageOrderValue(filtered.isEmpty() ? 0.0 : roundTwo(totalRevenue / filtered.size()));
        report.setTransactions(txList);

        // Top products
        List<ReportDto.TopProductItem> topProducts = prodUnitsMap.entrySet().stream()
                .map(e -> {
                    Product p = productMap.get(e.getKey());
                    int units = e.getValue()[0];
                    double pRev = roundTwo(e.getValue()[1] / 100.0);
                    double avgPrice = units > 0 ? roundTwo(pRev / units) : 0.0;
                    return new ReportDto.TopProductItem(e.getKey(), p != null ? p.getName() : "Product #" + e.getKey(), units, pRev, avgPrice);
                })
                .sorted(Comparator.comparing(ReportDto.TopProductItem::getUnitsSold).reversed())
                .limit(10)
                .collect(Collectors.toList());
        report.setTopProducts(topProducts);

        return report;
    }

    // =========================================================================
    // 2. PURCHASE REPORT
    // =========================================================================

    public ReportDto.PurchaseReport getPurchaseReport(String startDateStr, String endDateStr, String preset) {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        Company company = companyService.getCompanyById(companyId);
        DateRangeBounds bounds = resolveDateRange(startDateStr, endDateStr, preset);

        List<Purchase> allPurchases = purchaseRepository.findByCompanyId(companyId);
        List<Supplier> suppliers = supplierRepository.findByCompanyId(companyId);
        Map<Long, Supplier> supplierMap = suppliers.stream()
                .collect(Collectors.toMap(Supplier::getId, Function.identity(), (a, b) -> a));

        List<Purchase> filtered = allPurchases.stream().filter(p -> {
            OffsetDateTime pTime = p.getReceivedAt() != null ? p.getReceivedAt() : p.getCreatedAt();
            if (pTime == null) return false;
            if (bounds.startDateTime != null && pTime.isBefore(bounds.startDateTime)) return false;
            if (bounds.endDateTime != null && pTime.isAfter(bounds.endDateTime)) return false;
            return true;
        }).sorted(Comparator.comparing(Purchase::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        ReportDto.PurchaseReport report = new ReportDto.PurchaseReport();
        report.setCompanyName(company.getName());
        report.setFilter(toFilterInfo(bounds));

        double totalProcurementSpending = 0.0;
        int totalPurchasesCount = filtered.size();
        int receivedCount = 0;
        int pendingCount = 0;
        double pendingAmount = 0.0;
        int cancelledCount = 0;

        Map<Long, Object[]> supplierAgg = new HashMap<>(); // [orders, received, spending, lastDate]
        List<ReportDto.PurchaseReportItem> purchaseList = new ArrayList<>();

        for (Purchase p : filtered) {
            String status = p.getStatus() != null ? p.getStatus().trim().toUpperCase() : "PENDING";
            double amt = p.getTotalAmount() != null ? p.getTotalAmount() : 0.0;
            Supplier sup = supplierMap.get(p.getSupplierId());

            if ("RECEIVED".equals(status)) {
                totalProcurementSpending += amt;
                receivedCount++;
            } else if ("PENDING".equals(status)) {
                pendingCount++;
                pendingAmount += amt;
            } else if ("CANCELLED".equals(status)) {
                cancelledCount++;
            }

            supplierAgg.putIfAbsent(p.getSupplierId(), new Object[]{0, 0, 0.0, ""});
            Object[] sAgg = supplierAgg.get(p.getSupplierId());
            sAgg[0] = (Integer) sAgg[0] + 1;
            if ("RECEIVED".equals(status)) {
                sAgg[1] = (Integer) sAgg[1] + 1;
                sAgg[2] = (Double) sAgg[2] + amt;
            }
            String pDate = p.getCreatedAt() != null ? p.getCreatedAt().format(DATE_FMT) : "";
            if (pDate.compareTo((String) sAgg[3]) > 0) {
                sAgg[3] = pDate;
            }

            ReportDto.PurchaseReportItem item = new ReportDto.PurchaseReportItem();
            item.setId(p.getId());
            item.setPurchaseNumber(p.getPurchaseNumber() != null ? p.getPurchaseNumber() : "PO-" + p.getId());
            item.setSupplierName(sup != null ? sup.getName() : "Supplier #" + p.getSupplierId());
            item.setStatus(status);
            item.setTotalAmount(amt);
            item.setCreatedAt(p.getCreatedAt() != null ? p.getCreatedAt().format(DATE_FMT) : "");
            item.setReceivedAt(p.getReceivedAt() != null ? p.getReceivedAt().format(DATE_FMT) : "-");
            purchaseList.add(item);
        }

        report.setTotalProcurementSpending(roundTwo(totalProcurementSpending));
        report.setTotalPurchasesCount(totalPurchasesCount);
        report.setReceivedPurchasesCount(receivedCount);
        report.setPendingPurchasesCount(pendingCount);
        report.setPendingAmount(roundTwo(pendingAmount));
        report.setCancelledPurchasesCount(cancelledCount);
        report.setPurchases(purchaseList);

        // Supplier breakdown
        List<ReportDto.SupplierPurchaseItem> supplierItems = supplierAgg.entrySet().stream()
                .map(e -> {
                    Supplier s = supplierMap.get(e.getKey());
                    Object[] a = e.getValue();
                    ReportDto.SupplierPurchaseItem item = new ReportDto.SupplierPurchaseItem();
                    item.setSupplierId(e.getKey());
                    item.setSupplierName(s != null ? s.getName() : "Supplier #" + e.getKey());
                    item.setContactPerson(s != null && s.getContactPerson() != null ? s.getContactPerson() : "-");
                    item.setPhone(s != null && s.getPhone() != null ? s.getPhone() : "-");
                    item.setTotalOrders((Integer) a[0]);
                    item.setReceivedOrders((Integer) a[1]);
                    item.setTotalSpending(roundTwo((Double) a[2]));
                    item.setLastOrderDate((String) a[3]);
                    return item;
                })
                .sorted(Comparator.comparing(ReportDto.SupplierPurchaseItem::getTotalSpending).reversed())
                .collect(Collectors.toList());
        report.setSupplierBreakdown(supplierItems);

        return report;
    }

    // =========================================================================
    // 3. INVENTORY REPORT
    // =========================================================================

    public ReportDto.InventoryReport getInventoryReport() {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        Company company = companyService.getCompanyById(companyId);

        List<Product> products = productRepository.findByCompanyId(companyId);

        ReportDto.InventoryReport report = new ReportDto.InventoryReport();
        report.setCompanyName(company.getName());
        report.setGeneratedDate(LocalDate.now(ZoneOffset.UTC).format(DATE_FMT));

        int totalUnits = 0;
        double totalValuationCost = 0.0;
        double totalValuationRetail = 0.0;
        int lowStockCount = 0;
        int outOfStockCount = 0;

        List<ReportDto.ProductInventoryItem> productItems = new ArrayList<>();
        List<ReportDto.ProductInventoryItem> lowStockAlerts = new ArrayList<>();

        for (Product p : products) {
            int qty = p.getQuantity() != null ? p.getQuantity() : 0;
            double cost = p.getCostPrice() != null ? p.getCostPrice() : 0.0;
            double retail = p.getPrice() != null ? p.getPrice() : 0.0;
            int reorder = p.getReorderLevel() != null ? p.getReorderLevel() : 5;

            double valCost = roundTwo(cost * qty);
            double valRetail = roundTwo(retail * qty);

            totalUnits += qty;
            totalValuationCost += valCost;
            totalValuationRetail += valRetail;

            String status = "IN_STOCK";
            if (qty <= 0) {
                status = "OUT_OF_STOCK";
                outOfStockCount++;
            } else if (qty <= reorder) {
                status = "LOW_STOCK";
                lowStockCount++;
            }

            ReportDto.ProductInventoryItem item = new ReportDto.ProductInventoryItem();
            item.setId(p.getId());
            item.setName(p.getName());
            item.setCostPrice(cost);
            item.setSellingPrice(retail);
            item.setQuantity(qty);
            item.setReorderLevel(reorder);
            item.setValuationCost(valCost);
            item.setValuationRetail(valRetail);
            item.setStockStatus(status);

            productItems.add(item);
            if (!"IN_STOCK".equals(status)) {
                lowStockAlerts.add(item);
            }
        }

        report.setTotalProducts(products.size());
        report.setTotalStockUnits(totalUnits);
        report.setTotalValuationCost(roundTwo(totalValuationCost));
        report.setTotalValuationRetail(roundTwo(totalValuationRetail));
        report.setPotentialGrossMargin(roundTwo(totalValuationRetail - totalValuationCost));
        report.setLowStockCount(lowStockCount);
        report.setOutOfStockCount(outOfStockCount);
        report.setProducts(productItems);
        report.setLowStockAlerts(lowStockAlerts);

        return report;
    }

    // =========================================================================
    // 4. EXPENSE REPORT
    // =========================================================================

    public ReportDto.ExpenseReport getExpenseReport(String startDateStr, String endDateStr, String preset) {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        Company company = companyService.getCompanyById(companyId);
        DateRangeBounds bounds = resolveDateRange(startDateStr, endDateStr, preset);

        List<Expense> allExpenses = expenseRepository.findByCompanyId(companyId);

        List<Expense> filtered = allExpenses.stream().filter(exp -> {
            if (exp.getExpenseDate() == null) return false;
            if (bounds.startDate != null && exp.getExpenseDate().isBefore(bounds.startDate)) return false;
            if (bounds.endDate != null && exp.getExpenseDate().isAfter(bounds.endDate)) return false;
            return true;
        }).sorted(Comparator.comparing(Expense::getExpenseDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        ReportDto.ExpenseReport report = new ReportDto.ExpenseReport();
        report.setCompanyName(company.getName());
        report.setFilter(toFilterInfo(bounds));

        double totalExpenses = 0.0;
        Map<String, double[]> catMap = new HashMap<>(); // [amount, count]
        Map<String, double[]> monthMap = new HashMap<>(); // [amount, count]
        List<ReportDto.ExpenseReportItem> expenseList = new ArrayList<>();

        for (Expense exp : filtered) {
            double amt = exp.getAmount() != null ? exp.getAmount() : 0.0;
            totalExpenses += amt;

            String cat = StringUtils.hasText(exp.getCategory()) ? exp.getCategory().trim() : "Others";
            catMap.putIfAbsent(cat, new double[]{0.0, 0.0});
            double[] cAgg = catMap.get(cat);
            cAgg[0] += amt;
            cAgg[1] += 1;

            if (exp.getExpenseDate() != null) {
                String mKey = exp.getExpenseDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                monthMap.putIfAbsent(mKey, new double[]{0.0, 0.0});
                double[] mAgg = monthMap.get(mKey);
                mAgg[0] += amt;
                mAgg[1] += 1;
            }

            ReportDto.ExpenseReportItem item = new ReportDto.ExpenseReportItem();
            item.setId(exp.getId());
            item.setName(exp.getTitle() != null ? exp.getTitle() : "Expense #" + exp.getId());
            item.setCategory(cat);
            item.setAmount(amt);
            item.setExpenseDate(exp.getExpenseDate() != null ? exp.getExpenseDate().format(DATE_FMT) : "");
            item.setPaymentMethod(exp.getPaymentMethod() != null ? exp.getPaymentMethod() : "Cash");
            item.setDescription(exp.getDescription() != null ? exp.getDescription() : "");
            expenseList.add(item);
        }

        final double finalTotal = totalExpenses;
        report.setTotalExpenses(roundTwo(totalExpenses));
        report.setTotalTransactions(filtered.size());
        report.setAverageExpense(filtered.isEmpty() ? 0.0 : roundTwo(totalExpenses / filtered.size()));
        report.setExpenses(expenseList);

        // Category breakdown
        List<ReportDto.CategoryExpenseItem> catItems = catMap.entrySet().stream()
                .map(e -> {
                    double amt = roundTwo(e.getValue()[0]);
                    int count = (int) e.getValue()[1];
                    double pct = finalTotal > 0 ? roundTwo((amt / finalTotal) * 100.0) : 0.0;
                    return new ReportDto.CategoryExpenseItem(e.getKey(), amt, count, pct);
                })
                .sorted(Comparator.comparing(ReportDto.CategoryExpenseItem::getAmount).reversed())
                .collect(Collectors.toList());
        report.setCategoryBreakdown(catItems);

        // Monthly breakdown
        List<ReportDto.MonthlyExpenseItem> monthItems = monthMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    String ym = e.getKey();
                    String label = ym;
                    try {
                        label = YearMonth.parse(ym).format(MONTH_LABEL_FMT);
                    } catch (Exception ignored) {}
                    return new ReportDto.MonthlyExpenseItem(ym, label, roundTwo(e.getValue()[0]), (int) e.getValue()[1]);
                })
                .collect(Collectors.toList());
        report.setMonthlyBreakdown(monthItems);

        return report;
    }

    // =========================================================================
    // 5. PROFIT & LOSS REPORT (Calculations strictly match FinanceService)
    // =========================================================================

    public ReportDto.ProfitLossReport getProfitLossReport(String startDateStr, String endDateStr, String preset) {
        // Enforce RBAC
        SecurityUtils.requireAnyRole("COMPANY_ADMIN", "MANAGER");
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        Company company = companyService.getCompanyById(companyId);
        DateRangeBounds bounds = resolveDateRange(startDateStr, endDateStr, preset);

        List<Sale> allSales = saleRepository.findByCompanyId(companyId);
        List<Product> products = productRepository.findByCompanyId(companyId);
        List<Expense> allExpenses = expenseRepository.findByCompanyId(companyId);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));

        // Filter Sales
        List<Sale> filteredSales = allSales.stream().filter(sale -> {
            if (sale.getCreatedAt() == null) return false;
            if (bounds.startDateTime != null && sale.getCreatedAt().isBefore(bounds.startDateTime)) return false;
            if (bounds.endDateTime != null && sale.getCreatedAt().isAfter(bounds.endDateTime)) return false;
            return true;
        }).collect(Collectors.toList());

        // Filter Expenses
        List<Expense> filteredExpenses = allExpenses.stream().filter(exp -> {
            if (exp.getExpenseDate() == null) return false;
            if (bounds.startDate != null && exp.getExpenseDate().isBefore(bounds.startDate)) return false;
            if (bounds.endDate != null && exp.getExpenseDate().isAfter(bounds.endDate)) return false;
            return true;
        }).collect(Collectors.toList());

        // 1. Revenue & COGS
        double totalRevenue = 0.0;
        double totalCogs = 0.0;
        Map<String, double[]> monthlyMap = new TreeMap<>(); // ym -> [rev, cogs, exp]

        for (Sale sale : filteredSales) {
            double rev = sale.getTotalAmount() != null ? sale.getTotalAmount() : 0.0;
            int qty = sale.getQuantity() != null ? sale.getQuantity() : 0;
            totalRevenue += rev;

            Product prod = productMap.get(sale.getProductId());
            double costPrice = (prod != null && prod.getCostPrice() != null) ? prod.getCostPrice() : 0.0;
            double saleCogs = costPrice * qty;
            totalCogs += saleCogs;

            if (sale.getCreatedAt() != null) {
                String ym = sale.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                monthlyMap.putIfAbsent(ym, new double[]{0.0, 0.0, 0.0});
                monthlyMap.get(ym)[0] += rev;
                monthlyMap.get(ym)[1] += saleCogs;
            }
        }

        double grossProfit = totalRevenue - totalCogs;
        double grossProfitMargin = totalRevenue > 0 ? (grossProfit / totalRevenue) * 100.0 : 0.0;

        // 2. Operating Expenses
        double totalOperatingExpenses = 0.0;
        Map<String, double[]> catMap = new HashMap<>();

        for (Expense exp : filteredExpenses) {
            double amt = exp.getAmount() != null ? exp.getAmount() : 0.0;
            totalOperatingExpenses += amt;

            String cat = StringUtils.hasText(exp.getCategory()) ? exp.getCategory().trim() : "Others";
            catMap.putIfAbsent(cat, new double[]{0.0, 0.0});
            catMap.get(cat)[0] += amt;
            catMap.get(cat)[1] += 1;

            if (exp.getExpenseDate() != null) {
                String ym = exp.getExpenseDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                monthlyMap.putIfAbsent(ym, new double[]{0.0, 0.0, 0.0});
                monthlyMap.get(ym)[2] += amt;
            }
        }

        // 3. Net Operating Profit = Gross Profit - Operating Expenses
        double netOperatingProfit = grossProfit - totalOperatingExpenses;
        double netOperatingProfitMargin = totalRevenue > 0 ? (netOperatingProfit / totalRevenue) * 100.0 : 0.0;

        ReportDto.ProfitLossReport report = new ReportDto.ProfitLossReport();
        report.setCompanyName(company.getName());
        report.setFilter(toFilterInfo(bounds));
        report.setGrossSalesRevenue(roundTwo(totalRevenue));
        report.setCostOfGoodsSold(roundTwo(totalCogs));
        report.setGrossProfit(roundTwo(grossProfit));
        report.setGrossProfitMarginPct(roundTwo(grossProfitMargin));
        report.setTotalOperatingExpenses(roundTwo(totalOperatingExpenses));
        report.setNetOperatingProfit(roundTwo(netOperatingProfit));
        report.setNetOperatingProfitMarginPct(roundTwo(netOperatingProfitMargin));

        final double finalExpTotal = totalOperatingExpenses;
        List<ReportDto.CategoryExpenseItem> catBreakdown = catMap.entrySet().stream()
                .map(e -> {
                    double amt = roundTwo(e.getValue()[0]);
                    int count = (int) e.getValue()[1];
                    double pct = finalExpTotal > 0 ? roundTwo((amt / finalExpTotal) * 100.0) : 0.0;
                    return new ReportDto.CategoryExpenseItem(e.getKey(), amt, count, pct);
                })
                .sorted(Comparator.comparing(ReportDto.CategoryExpenseItem::getAmount).reversed())
                .collect(Collectors.toList());
        report.setOperatingExpensesByCategory(catBreakdown);

        // Monthly comparison
        List<ReportDto.MonthlyProfitLossItem> monthlyList = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : monthlyMap.entrySet()) {
            String ym = entry.getKey();
            String label = ym;
            try {
                label = YearMonth.parse(ym).format(MONTH_LABEL_FMT);
            } catch (Exception ignored) {}

            double mRev = roundTwo(entry.getValue()[0]);
            double mCogs = roundTwo(entry.getValue()[1]);
            double mGross = roundTwo(mRev - mCogs);
            double mExp = roundTwo(entry.getValue()[2]);
            double mNet = roundTwo(mGross - mExp);
            double mMargin = mRev > 0 ? roundTwo((mNet / mRev) * 100.0) : 0.0;

            ReportDto.MonthlyProfitLossItem mItem = new ReportDto.MonthlyProfitLossItem();
            mItem.setMonth(ym);
            mItem.setMonthLabel(label);
            mItem.setRevenue(mRev);
            mItem.setCogs(mCogs);
            mItem.setGrossProfit(mGross);
            mItem.setOperatingExpenses(mExp);
            mItem.setNetOperatingProfit(mNet);
            mItem.setNetMarginPct(mMargin);
            monthlyList.add(mItem);
        }
        report.setMonthlyComparison(monthlyList);

        return report;
    }

    // =========================================================================
    // 6. RECEIVABLES REPORT
    // =========================================================================

    public ReportDto.ReceivablesReport getReceivablesReport(String startDateStr, String endDateStr, String preset) {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        Company company = companyService.getCompanyById(companyId);
        DateRangeBounds bounds = resolveDateRange(startDateStr, endDateStr, preset);

        List<Sale> allSales = saleRepository.findByCompanyId(companyId);
        List<Customer> customers = customerRepository.findByCompanyId(companyId);
        Map<Long, Customer> customerMap = customers.stream()
                .collect(Collectors.toMap(Customer::getId, Function.identity(), (a, b) -> a));

        List<Sale> filtered = allSales.stream().filter(sale -> {
            if (sale.getCreatedAt() == null) return false;
            if (bounds.startDateTime != null && sale.getCreatedAt().isBefore(bounds.startDateTime)) return false;
            if (bounds.endDateTime != null && sale.getCreatedAt().isAfter(bounds.endDateTime)) return false;
            return true;
        }).sorted(Comparator.comparing(Sale::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        ReportDto.ReceivablesReport report = new ReportDto.ReceivablesReport();
        report.setCompanyName(company.getName());
        report.setFilter(toFilterInfo(bounds));

        double totalInvoiced = 0.0;
        double totalPaid = 0.0;
        double totalReceivables = 0.0;
        double totalOverdue = 0.0;
        int paidCount = 0;
        int partialCount = 0;
        int unpaidCount = 0;
        int overdueCount = 0;

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Map<Long, double[]> custAgg = new HashMap<>(); // [invoiced, paid, balance, overdue]
        List<ReportDto.InvoiceReceivableItem> invoiceList = new ArrayList<>();

        for (Sale s : filtered) {
            double total = s.getTotalAmount() != null ? s.getTotalAmount() : 0.0;
            double paid = s.getPaidAmount() != null ? s.getPaidAmount() : 0.0;
            double balance = s.getBalanceAmount() != null ? s.getBalanceAmount() : Math.max(0.0, total - paid);
            String status = s.getPaymentStatus() != null ? s.getPaymentStatus() : (balance <= 0.001 ? "PAID" : "UNPAID");

            boolean isOverdue = balance > 0.001 && s.getDueDate() != null && s.getDueDate().isBefore(today);

            totalInvoiced += total;
            totalPaid += paid;
            totalReceivables += balance;
            if (isOverdue) {
                totalOverdue += balance;
                overdueCount++;
            }

            if ("PAID".equalsIgnoreCase(status)) {
                paidCount++;
            } else if ("PARTIALLY_PAID".equalsIgnoreCase(status)) {
                partialCount++;
            } else {
                unpaidCount++;
            }

            if (s.getCustomerId() != null) {
                custAgg.putIfAbsent(s.getCustomerId(), new double[]{0.0, 0.0, 0.0, 0.0});
                double[] cAgg = custAgg.get(s.getCustomerId());
                cAgg[0] += total;
                cAgg[1] += paid;
                cAgg[2] += balance;
                if (isOverdue) cAgg[3] += balance;
            }

            Customer cust = s.getCustomerId() != null ? customerMap.get(s.getCustomerId()) : null;

            ReportDto.InvoiceReceivableItem item = new ReportDto.InvoiceReceivableItem();
            item.setId(s.getId());
            item.setInvoiceNumber(s.getInvoiceNumber() != null ? s.getInvoiceNumber() : "INV-" + s.getId());
            item.setSaleDate(s.getCreatedAt() != null ? s.getCreatedAt().format(DATE_FMT) : "");
            item.setDueDate(s.getDueDate() != null ? s.getDueDate().format(DATE_FMT) : "-");
            item.setCustomerName(cust != null ? cust.getName() : "Direct Customer");
            item.setTotalAmount(roundTwo(total));
            item.setPaidAmount(roundTwo(paid));
            item.setBalanceAmount(roundTwo(balance));
            item.setPaymentStatus(status);
            item.setIsOverdue(isOverdue);
            invoiceList.add(item);
        }

        report.setTotalInvoiced(roundTwo(totalInvoiced));
        report.setTotalPaid(roundTwo(totalPaid));
        report.setTotalReceivables(roundTwo(totalReceivables));
        report.setTotalOverdue(roundTwo(totalOverdue));
        report.setCollectionRatePct(totalInvoiced > 0 ? roundTwo((totalPaid / totalInvoiced) * 100.0) : 0.0);
        report.setPaidCount(paidCount);
        report.setPartiallyPaidCount(partialCount);
        report.setUnpaidCount(unpaidCount);
        report.setOverdueCount(overdueCount);
        report.setInvoices(invoiceList);

        // Customer breakdown
        List<ReportDto.CustomerReceivableItem> customerItems = custAgg.entrySet().stream()
                .map(e -> {
                    Customer c = customerMap.get(e.getKey());
                    double[] a = e.getValue();
                    ReportDto.CustomerReceivableItem item = new ReportDto.CustomerReceivableItem();
                    item.setCustomerId(e.getKey());
                    item.setCustomerName(c != null ? c.getName() : "Customer #" + e.getKey());
                    item.setPhone(c != null && c.getPhone() != null ? c.getPhone() : "-");
                    item.setTotalInvoiced(roundTwo(a[0]));
                    item.setTotalPaid(roundTwo(a[1]));
                    item.setOutstandingBalance(roundTwo(a[2]));
                    item.setOverdueBalance(roundTwo(a[3]));
                    item.setStatus(a[2] <= 0.001 ? "CLEAR" : (a[3] > 0.001 ? "OVERDUE" : "OUTSTANDING"));
                    return item;
                })
                .sorted(Comparator.comparing(ReportDto.CustomerReceivableItem::getOutstandingBalance).reversed())
                .collect(Collectors.toList());
        report.setCustomerBreakdown(customerItems);

        return report;
    }

    // =========================================================================
    // EXPORT DISPATCHER (PDF / EXCEL)
    // =========================================================================

    public byte[] exportReport(String reportType, String format, String startDateStr, String endDateStr, String preset) {
        String type = reportType != null ? reportType.trim().toLowerCase() : "sales";
        String fmt = format != null ? format.trim().toLowerCase() : "pdf";

        Object reportData;
        switch (type) {
            case "sales":
                reportData = getSalesReport(startDateStr, endDateStr, preset);
                break;
            case "purchases":
            case "purchase":
                reportData = getPurchaseReport(startDateStr, endDateStr, preset);
                break;
            case "inventory":
                reportData = getInventoryReport();
                break;
            case "expenses":
            case "expense":
                reportData = getExpenseReport(startDateStr, endDateStr, preset);
                break;
            case "profit-loss":
            case "profit_loss":
                reportData = getProfitLossReport(startDateStr, endDateStr, preset);
                break;
            case "receivables":
            case "receivable":
                reportData = getReceivablesReport(startDateStr, endDateStr, preset);
                break;
            default:
                throw new BadRequestException("Unsupported report type: " + reportType);
        }

        if ("excel".equalsIgnoreCase(fmt) || "xlsx".equalsIgnoreCase(fmt) || "xls".equalsIgnoreCase(fmt)) {
            return reportExportService.generateExcel(type, reportData);
        } else if ("pdf".equalsIgnoreCase(fmt)) {
            return reportExportService.generatePdf(type, reportData);
        } else {
            throw new BadRequestException("Unsupported export format: " + format + ". Supported: pdf, excel");
        }
    }

    private double roundTwo(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
