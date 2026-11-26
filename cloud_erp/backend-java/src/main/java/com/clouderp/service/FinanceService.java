package com.clouderp.service;

import com.clouderp.config.SecurityUtils;
import com.clouderp.dto.FinanceDto;
import com.clouderp.exception.BadRequestException;
import com.clouderp.model.Company;
import com.clouderp.model.Expense;
import com.clouderp.model.Product;
import com.clouderp.model.Purchase;
import com.clouderp.model.Sale;
import com.clouderp.repository.ExpenseRepository;
import com.clouderp.repository.ProductRepository;
import com.clouderp.repository.PurchaseRepository;
import com.clouderp.repository.SaleRepository;
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
public class FinanceService {

    private final CompanyService companyService;
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final ExpenseRepository expenseRepository;
    private final PurchaseRepository purchaseRepository;

    private static final DateTimeFormatter MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    public FinanceService(CompanyService companyService,
                          SaleRepository saleRepository,
                          ProductRepository productRepository,
                          ExpenseRepository expenseRepository,
                          PurchaseRepository purchaseRepository) {
        this.companyService = companyService;
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.expenseRepository = expenseRepository;
        this.purchaseRepository = purchaseRepository;
    }

    public FinanceDto.SummaryResponse getFinancialSummary(String startDateStr, String endDateStr) {
        // Enforce RBAC: only COMPANY_ADMIN and MANAGER can access financial analytics
        SecurityUtils.requireAnyRole("COMPANY_ADMIN", "MANAGER");
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        Company company = companyService.getCompanyById(companyId);

        // Date-Range Filters
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (StringUtils.hasText(startDateStr)) {
            try {
                startDate = LocalDate.parse(startDateStr.trim());
            } catch (Exception e) {
                throw new BadRequestException("Invalid startDate format. Expected YYYY-MM-DD");
            }
        }

        if (StringUtils.hasText(endDateStr)) {
            try {
                endDate = LocalDate.parse(endDateStr.trim());
            } catch (Exception e) {
                throw new BadRequestException("Invalid endDate format. Expected YYYY-MM-DD");
            }
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("startDate cannot be after endDate");
        }

        OffsetDateTime startDateTime = startDate != null ? startDate.atStartOfDay().atOffset(ZoneOffset.UTC) : null;
        OffsetDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59, 999999999).atOffset(ZoneOffset.UTC) : null;

        final LocalDate finalStartDate = startDate;
        final LocalDate finalEndDate = endDate;
        final OffsetDateTime finalStartDateTime = startDateTime;
        final OffsetDateTime finalEndDateTime = endDateTime;

        // Fetch company-isolated records
        List<Sale> allSales = saleRepository.findByCompanyId(companyId);
        List<Product> products = productRepository.findByCompanyId(companyId);
        List<Expense> allExpenses = expenseRepository.findByCompanyId(companyId);
        List<Purchase> allPurchases = purchaseRepository.findByCompanyId(companyId);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));

        // Filter Sales by Date
        List<Sale> filteredSales = allSales.stream().filter(sale -> {
            if (sale.getCreatedAt() == null) return false;
            if (finalStartDateTime != null && sale.getCreatedAt().isBefore(finalStartDateTime)) return false;
            if (finalEndDateTime != null && sale.getCreatedAt().isAfter(finalEndDateTime)) return false;
            return true;
        }).collect(Collectors.toList());

        // Filter Expenses by Date
        List<Expense> filteredExpenses = allExpenses.stream().filter(exp -> {
            if (exp.getExpenseDate() == null) return false;
            if (finalStartDate != null && exp.getExpenseDate().isBefore(finalStartDate)) return false;
            if (finalEndDate != null && exp.getExpenseDate().isAfter(finalEndDate)) return false;
            return true;
        }).collect(Collectors.toList());

        // Filter Purchases by Date
        List<Purchase> filteredPurchases = allPurchases.stream().filter(p -> {
            OffsetDateTime pTime = p.getReceivedAt() != null ? p.getReceivedAt() : p.getCreatedAt();
            if (pTime == null) return false;
            if (finalStartDateTime != null && pTime.isBefore(finalStartDateTime)) return false;
            if (finalEndDateTime != null && pTime.isAfter(finalEndDateTime)) return false;
            return true;
        }).collect(Collectors.toList());

        // 1. REVENUE & COGS CALCULATION
        double totalRevenue = 0.0;
        int totalUnitsSold = 0;
        double totalCogs = 0.0;

        Map<Long, double[]> productSalesAgg = new HashMap<>(); // [units, revenue, cogs]

        for (Sale sale : filteredSales) {
            double rev = sale.getTotalAmount() != null ? sale.getTotalAmount() : 0.0;
            int qty = sale.getQuantity() != null ? sale.getQuantity() : 0;
            totalRevenue += rev;
            totalUnitsSold += qty;

            Product prod = productMap.get(sale.getProductId());
            double costPrice = (prod != null && prod.getCostPrice() != null) ? prod.getCostPrice() : 0.0;
            double saleCogs = costPrice * qty;
            totalCogs += saleCogs;

            productSalesAgg.putIfAbsent(sale.getProductId(), new double[]{0.0, 0.0, 0.0});
            double[] pAgg = productSalesAgg.get(sale.getProductId());
            pAgg[0] += qty;
            pAgg[1] += rev;
            pAgg[2] += saleCogs;
        }

        // Gross Profit = Revenue - COGS
        double grossProfit = totalRevenue - totalCogs;
        double grossProfitMargin = totalRevenue > 0 ? (grossProfit / totalRevenue) * 100.0 : 0.0;

        // Receivables calculation from sales
        double totalReceivables = 0.0;
        double totalOverdueReceivables = 0.0;
        LocalDate todayDate = LocalDate.now(ZoneOffset.UTC);

        for (Sale sale : filteredSales) {
            double bal = sale.getBalanceAmount() != null ? sale.getBalanceAmount() : Math.max(0.0, sale.getTotalAmount() - (sale.getPaidAmount() != null ? sale.getPaidAmount() : 0.0));
            if (bal > 0.001) {
                totalReceivables += bal;
                if (sale.getDueDate() != null && sale.getDueDate().isBefore(todayDate)) {
                    totalOverdueReceivables += bal;
                }
            }
        }

        // 2. OPERATING EXPENSES CALCULATION
        double totalOperatingExpenses = 0.0;
        Map<String, double[]> categoryExpenseAgg = new HashMap<>(); // [amount, count]

        for (Expense exp : filteredExpenses) {
            double amt = exp.getAmount() != null ? exp.getAmount() : 0.0;
            totalOperatingExpenses += amt;

            String cat = StringUtils.hasText(exp.getCategory()) ? exp.getCategory().trim() : "Others";
            categoryExpenseAgg.putIfAbsent(cat, new double[]{0.0, 0.0});
            double[] cAgg = categoryExpenseAgg.get(cat);
            cAgg[0] += amt;
            cAgg[1] += 1;
        }

        // Net Operating Profit = Gross Profit - Operating Expenses
        double netOperatingProfit = grossProfit - totalOperatingExpenses;
        double netOperatingProfitMargin = totalRevenue > 0 ? (netOperatingProfit / totalRevenue) * 100.0 : 0.0;

        // 3. PROCUREMENT & PURCHASES (Received only for procurement spending)
        double procurementSpending = 0.0;
        int procurementCount = 0;
        double pendingPurchasesAmount = 0.0;
        int pendingPurchasesCount = 0;
        int cancelledPurchasesCount = 0;

        for (Purchase p : filteredPurchases) {
            String status = p.getStatus() != null ? p.getStatus().trim().toUpperCase() : "PENDING";
            double amt = p.getTotalAmount() != null ? p.getTotalAmount() : 0.0;

            if ("RECEIVED".equals(status)) {
                procurementSpending += amt;
                procurementCount++;
            } else if ("PENDING".equals(status)) {
                pendingPurchasesAmount += amt;
                pendingPurchasesCount++;
            } else if ("CANCELLED".equals(status)) {
                cancelledPurchasesCount++;
            }
        }

        // 4. MONTHLY REVENUE VS EXPENSES TIMELINE
        List<YearMonth> timelineMonths = buildTimelineMonths(startDate, endDate, filteredSales, filteredExpenses, filteredPurchases);
        Map<YearMonth, MonthlyBucket> monthlyBuckets = new LinkedHashMap<>();
        for (YearMonth ym : timelineMonths) {
            monthlyBuckets.put(ym, new MonthlyBucket());
        }

        // Populate monthly sales
        for (Sale sale : filteredSales) {
            YearMonth ym = YearMonth.from(sale.getCreatedAt());
            MonthlyBucket bucket = monthlyBuckets.get(ym);
            if (bucket != null) {
                double rev = sale.getTotalAmount() != null ? sale.getTotalAmount() : 0.0;
                int qty = sale.getQuantity() != null ? sale.getQuantity() : 0;
                Product prod = productMap.get(sale.getProductId());
                double cost = (prod != null && prod.getCostPrice() != null) ? prod.getCostPrice() : 0.0;

                bucket.revenue += rev;
                bucket.cogs += (cost * qty);
            }
        }

        // Populate monthly expenses
        for (Expense exp : filteredExpenses) {
            YearMonth ym = YearMonth.from(exp.getExpenseDate());
            MonthlyBucket bucket = monthlyBuckets.get(ym);
            if (bucket != null) {
                bucket.operatingExpenses += exp.getAmount() != null ? exp.getAmount() : 0.0;
            }
        }

        // Populate monthly procurement
        for (Purchase p : filteredPurchases) {
            if ("RECEIVED".equalsIgnoreCase(p.getStatus())) {
                OffsetDateTime pTime = p.getReceivedAt() != null ? p.getReceivedAt() : p.getCreatedAt();
                if (pTime != null) {
                    YearMonth ym = YearMonth.from(pTime);
                    MonthlyBucket bucket = monthlyBuckets.get(ym);
                    if (bucket != null) {
                        bucket.procurementSpending += p.getTotalAmount() != null ? p.getTotalAmount() : 0.0;
                    }
                }
            }
        }

        List<FinanceDto.MonthlyFinancialItem> monthlyTrends = new ArrayList<>();
        FinanceDto.MonthPerformance bestRevenueMonth = null;
        FinanceDto.MonthPerformance lowestRevenueMonth = null;
        FinanceDto.MonthPerformance bestProfitMonth = null;
        FinanceDto.MonthPerformance lowestProfitMonth = null;

        for (Map.Entry<YearMonth, MonthlyBucket> entry : monthlyBuckets.entrySet()) {
            YearMonth ym = entry.getKey();
            MonthlyBucket b = entry.getValue();

            double mRevenue = round(b.revenue);
            double mCogs = round(b.cogs);
            double mGrossProfit = round(mRevenue - mCogs);
            double mExpenses = round(b.operatingExpenses);
            double mNetProfit = round(mGrossProfit - mExpenses);
            double mProcurement = round(b.procurementSpending);
            double mGrossMargin = mRevenue > 0 ? round((mGrossProfit / mRevenue) * 100.0) : 0.0;
            double mNetMargin = mRevenue > 0 ? round((mNetProfit / mRevenue) * 100.0) : 0.0;

            String monthKey = ym.toString();
            String label = ym.format(MONTH_LABEL_FORMATTER);

            FinanceDto.MonthlyFinancialItem item = new FinanceDto.MonthlyFinancialItem(
                    monthKey, label, mRevenue, mCogs, mGrossProfit, mExpenses, mNetProfit, mProcurement, mGrossMargin, mNetMargin
            );
            monthlyTrends.add(item);

            // Track Best & Lowest
            if (bestRevenueMonth == null || mRevenue > bestRevenueMonth.getAmount()) {
                bestRevenueMonth = new FinanceDto.MonthPerformance(monthKey, label, mRevenue);
            }
            if (lowestRevenueMonth == null || mRevenue < lowestRevenueMonth.getAmount()) {
                lowestRevenueMonth = new FinanceDto.MonthPerformance(monthKey, label, mRevenue);
            }
            if (bestProfitMonth == null || mNetProfit > bestProfitMonth.getAmount()) {
                bestProfitMonth = new FinanceDto.MonthPerformance(monthKey, label, mNetProfit);
            }
            if (lowestProfitMonth == null || mNetProfit < lowestProfitMonth.getAmount()) {
                lowestProfitMonth = new FinanceDto.MonthPerformance(monthKey, label, mNetProfit);
            }
        }

        // 5. EXPENSE CATEGORY BREAKDOWN
        List<FinanceDto.ExpenseCategoryItem> expenseBreakdown = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : categoryExpenseAgg.entrySet()) {
            String category = entry.getKey();
            double amt = round(entry.getValue()[0]);
            int count = (int) entry.getValue()[1];
            double pct = totalOperatingExpenses > 0 ? round((amt / totalOperatingExpenses) * 100.0) : 0.0;
            expenseBreakdown.add(new FinanceDto.ExpenseCategoryItem(category, amt, pct, count));
        }
        expenseBreakdown.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));

        // 6. PRODUCT PROFITABILITY BREAKDOWN
        List<FinanceDto.ProductProfitabilityItem> productProfitability = new ArrayList<>();
        for (Map.Entry<Long, double[]> entry : productSalesAgg.entrySet()) {
            Long prodId = entry.getKey();
            Product prod = productMap.get(prodId);
            String prodName = prod != null ? prod.getName() : "Product #" + prodId;

            int units = (int) entry.getValue()[0];
            double rev = round(entry.getValue()[1]);
            double c = round(entry.getValue()[2]);
            double gp = round(rev - c);
            double margin = rev > 0 ? round((gp / rev) * 100.0) : 0.0;

            productProfitability.add(new FinanceDto.ProductProfitabilityItem(
                    prodId, prodName, units, rev, c, gp, margin
            ));
        }
        productProfitability.sort((a, b) -> Double.compare(b.getGrossProfit(), a.getGrossProfit()));

        // Assemble Final Summary Response
        FinanceDto.SummaryResponse response = new FinanceDto.SummaryResponse();
        response.setCompanyId(company.getId());
        response.setCompanyName(company.getName());
        response.setCurrency("INR");
        response.setStartDate(startDate != null ? startDate.toString() : null);
        response.setEndDate(endDate != null ? endDate.toString() : null);

        response.setTotalRevenue(round(totalRevenue));
        response.setTotalSalesCount(filteredSales.size());
        response.setTotalUnitsSold(totalUnitsSold);

        response.setCogs(round(totalCogs));
        response.setGrossProfit(round(grossProfit));
        response.setGrossProfitMargin(round(grossProfitMargin));

        response.setTotalOperatingExpenses(round(totalOperatingExpenses));
        response.setExpenseCount(filteredExpenses.size());

        response.setNetOperatingProfit(round(netOperatingProfit));
        response.setNetOperatingProfitMargin(round(netOperatingProfitMargin));

        response.setProcurementSpending(round(procurementSpending));
        response.setProcurementCount(procurementCount);
        response.setPendingPurchasesAmount(round(pendingPurchasesAmount));
        response.setPendingPurchasesCount(pendingPurchasesCount);
        response.setCancelledPurchasesCount(cancelledPurchasesCount);

        response.setTotalReceivables(round(totalReceivables));
        response.setTotalOverdueReceivables(round(totalOverdueReceivables));

        response.setBestMonthRevenue(bestRevenueMonth != null ? bestRevenueMonth : new FinanceDto.MonthPerformance("N/A", "N/A", 0.0));
        response.setLowestMonthRevenue(lowestRevenueMonth != null ? lowestRevenueMonth : new FinanceDto.MonthPerformance("N/A", "N/A", 0.0));
        response.setBestMonthProfit(bestProfitMonth != null ? bestProfitMonth : new FinanceDto.MonthPerformance("N/A", "N/A", 0.0));
        response.setLowestMonthProfit(lowestProfitMonth != null ? lowestProfitMonth : new FinanceDto.MonthPerformance("N/A", "N/A", 0.0));

        response.setMonthlyTrends(monthlyTrends);
        response.setExpenseBreakdown(expenseBreakdown);
        response.setProductProfitability(productProfitability);

        return response;
    }

    private List<YearMonth> buildTimelineMonths(LocalDate startDate, LocalDate endDate,
                                                List<Sale> sales, List<Expense> expenses, List<Purchase> purchases) {
        if (startDate != null && endDate != null) {
            YearMonth startYm = YearMonth.from(startDate);
            YearMonth endYm = YearMonth.from(endDate);
            List<YearMonth> list = new ArrayList<>();
            YearMonth cur = startYm;
            while (!cur.isAfter(endYm)) {
                list.add(cur);
                cur = cur.plusMonths(1);
            }
            return list;
        }

        // Default: determine window spanning latest transactions or at least last 6 months
        YearMonth currentYm = YearMonth.now(ZoneOffset.UTC);
        Set<YearMonth> activeMonths = new TreeSet<>();

        for (int i = 5; i >= 0; i--) {
            activeMonths.add(currentYm.minusMonths(i));
        }

        for (Sale s : sales) {
            if (s.getCreatedAt() != null) activeMonths.add(YearMonth.from(s.getCreatedAt()));
        }
        for (Expense e : expenses) {
            if (e.getExpenseDate() != null) activeMonths.add(YearMonth.from(e.getExpenseDate()));
        }
        for (Purchase p : purchases) {
            OffsetDateTime pt = p.getReceivedAt() != null ? p.getReceivedAt() : p.getCreatedAt();
            if (pt != null) activeMonths.add(YearMonth.from(pt));
        }

        return new ArrayList<>(activeMonths);
    }

    private static double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private static class MonthlyBucket {
        double revenue = 0.0;
        double cogs = 0.0;
        double operatingExpenses = 0.0;
        double procurementSpending = 0.0;
    }
}
