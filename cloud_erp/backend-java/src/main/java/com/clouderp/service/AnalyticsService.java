package com.clouderp.service;

import com.clouderp.config.SecurityUtils;
import com.clouderp.dto.AnalyticsDto;
import com.clouderp.model.Company;
import com.clouderp.model.Customer;
import com.clouderp.model.Product;
import com.clouderp.model.Sale;
import com.clouderp.repository.CustomerRepository;
import com.clouderp.repository.ProductRepository;
import com.clouderp.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final CompanyService companyService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;

    private static final DateTimeFormatter MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    public AnalyticsService(CompanyService companyService,
                            ProductRepository productRepository,
                            CustomerRepository customerRepository,
                            SaleRepository saleRepository) {
        this.companyService = companyService;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
    }

    public AnalyticsDto.OverviewResponse getOverview(Long requestedCompanyId) {
        // Enforce company ID from authenticated user
        Long companyId = SecurityUtils.getCurrentCompanyId();
        Company company = companyService.getCompanyById(companyId);

        List<Product> products = productRepository.findByCompanyId(companyId);
        List<Customer> customers = customerRepository.findByCompanyId(companyId);
        List<Sale> sales = saleRepository.findByCompanyIdOrderByCreatedAtAsc(companyId);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<Long, Customer> customerMap = customers.stream()
                .collect(Collectors.toMap(Customer::getId, Function.identity()));

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        // 1. BASE TOTALS
        double totalRevenue = 0.0;
        int totalUnits = 0;
        int totalTransactions = sales.size();

        for (Sale sale : sales) {
            totalRevenue += sale.getTotalAmount() != null ? sale.getTotalAmount() : 0.0;
            totalUnits += sale.getQuantity() != null ? sale.getQuantity() : 0;
        }

        double averageOrderValue = totalTransactions > 0 ? (totalRevenue / totalTransactions) : 0.0;

        // 2. SALES TRENDS — LAST 6 CALENDAR MONTHS
        YearMonth currentYearMonth = YearMonth.from(now);
        List<YearMonth> last6Months = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            last6Months.add(currentYearMonth.minusMonths(i));
        }

        Map<String, double[]> trendLookup = new LinkedHashMap<>();
        for (YearMonth ym : last6Months) {
            trendLookup.put(ym.toString(), new double[]{0.0, 0.0, 0.0});
        }

        for (Sale sale : sales) {
            if (sale.getCreatedAt() == null) continue;
            YearMonth saleYm = YearMonth.from(sale.getCreatedAt());
            String key = saleYm.toString();

            if (trendLookup.containsKey(key)) {
                double[] stats = trendLookup.get(key);
                stats[0] += sale.getTotalAmount() != null ? sale.getTotalAmount() : 0.0;
                stats[1] += sale.getQuantity() != null ? sale.getQuantity() : 0;
                stats[2] += 1;
            }
        }

        List<AnalyticsDto.SalesTrendItem> salesTrends = new ArrayList<>();
        for (YearMonth ym : last6Months) {
            String key = ym.toString();
            double[] stats = trendLookup.get(key);
            String label = ym.format(MONTH_LABEL_FORMATTER);
            salesTrends.add(new AnalyticsDto.SalesTrendItem(
                    key,
                    label,
                    round(stats[0]),
                    (int) stats[1],
                    (int) stats[2]
            ));
        }

        double latestRevenue = salesTrends.get(salesTrends.size() - 1).getRevenue();
        double previousRevenue = salesTrends.get(salesTrends.size() - 2).getRevenue();

        double revenueGrowthPct;
        if (previousRevenue > 0) {
            revenueGrowthPct = ((latestRevenue - previousRevenue) / previousRevenue) * 100.0;
        } else {
            revenueGrowthPct = latestRevenue > 0 ? 100.0 : 0.0;
        }

        AnalyticsDto.SalesTrendItem bestTrend = salesTrends.stream()
                .max(Comparator.comparingDouble(AnalyticsDto.SalesTrendItem::getRevenue))
                .orElse(new AnalyticsDto.SalesTrendItem("N/A", "No data", 0.0, 0, 0));

        AnalyticsDto.RevenueAnalysis revenueAnalysis = new AnalyticsDto.RevenueAnalysis();
        revenueAnalysis.setTotalRevenue(round(totalRevenue));
        revenueAnalysis.setTotalUnits(totalUnits);
        revenueAnalysis.setTotalTransactions(totalTransactions);
        revenueAnalysis.setAverageOrderValue(round(averageOrderValue));
        revenueAnalysis.setLatestMonthRevenue(round(latestRevenue));
        revenueAnalysis.setPreviousMonthRevenue(round(previousRevenue));
        revenueAnalysis.setRevenueGrowthPct(round(revenueGrowthPct));
        revenueAnalysis.setBestMonth(bestTrend.getLabel());
        revenueAnalysis.setBestMonthRevenue(round(bestTrend.getRevenue()));

        // 3. TOP PRODUCTS & PRODUCT PROFITABILITY
        Map<Long, double[]> productStats = new HashMap<>();
        for (Sale sale : sales) {
            productStats.putIfAbsent(sale.getProductId(), new double[]{0.0, 0.0});
            double[] stats = productStats.get(sale.getProductId());
            stats[0] += sale.getQuantity() != null ? sale.getQuantity() : 0;
            stats[1] += sale.getTotalAmount() != null ? sale.getTotalAmount() : 0.0;
        }

        List<AnalyticsDto.TopProductItem> topProducts = new ArrayList<>();
        List<AnalyticsDto.ProfitabilityItem> profitability = new ArrayList<>();

        for (Map.Entry<Long, double[]> entry : productStats.entrySet()) {
            Long prodId = entry.getKey();
            Product prod = productMap.get(prodId);
            if (prod == null) continue;

            int unitsSold = (int) entry.getValue()[0];
            double revenue = entry.getValue()[1];

            topProducts.add(new AnalyticsDto.TopProductItem(
                    prod.getId(),
                    prod.getName(),
                    unitsSold,
                    round(revenue),
                    prod.getQuantity()
            ));

            double unitCost = prod.getCostPrice() != null ? prod.getCostPrice() : 0.0;
            double estimatedCost = unitCost * unitsSold;
            double grossProfit = revenue - estimatedCost;
            double marginPct = revenue > 0 ? (grossProfit / revenue) * 100.0 : 0.0;

            profitability.add(new AnalyticsDto.ProfitabilityItem(
                    prod.getId(),
                    prod.getName(),
                    unitsSold,
                    round(revenue),
                    round(estimatedCost),
                    round(grossProfit),
                    round(marginPct)
            ));
        }

        topProducts.sort((a, b) -> Double.compare(b.getRevenue(), a.getRevenue()));
        if (topProducts.size() > 10) {
            topProducts = topProducts.subList(0, 10);
        }

        profitability.sort((a, b) -> Double.compare(b.getGrossProfit(), a.getGrossProfit()));
        if (profitability.size() > 10) {
            profitability = profitability.subList(0, 10);
        }

        // 4. CUSTOMER ANALYSIS
        Map<Long, CustomerAccumulator> custStats = new HashMap<>();
        for (Sale sale : sales) {
            if (sale.getCustomerId() == null) continue;
            custStats.putIfAbsent(sale.getCustomerId(), new CustomerAccumulator());
            CustomerAccumulator acc = custStats.get(sale.getCustomerId());
            acc.transactions += 1;
            acc.units += sale.getQuantity() != null ? sale.getQuantity() : 0;
            acc.revenue += sale.getTotalAmount() != null ? sale.getTotalAmount() : 0.0;

            if (sale.getCreatedAt() != null) {
                if (acc.lastPurchase == null || sale.getCreatedAt().isAfter(acc.lastPurchase)) {
                    acc.lastPurchase = sale.getCreatedAt();
                }
            }
        }

        List<AnalyticsDto.CustomerItem> customerRows = new ArrayList<>();
        for (Map.Entry<Long, CustomerAccumulator> entry : custStats.entrySet()) {
            Customer cust = customerMap.get(entry.getKey());
            if (cust == null) continue;

            CustomerAccumulator acc = entry.getValue();
            Long daysSince = acc.lastPurchase != null
                    ? Math.max(0, ChronoUnit.DAYS.between(acc.lastPurchase, now))
                    : null;
            String lastPurchaseStr = acc.lastPurchase != null
                    ? acc.lastPurchase.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    : null;

            customerRows.add(new AnalyticsDto.CustomerItem(
                    cust.getId(),
                    cust.getName(),
                    acc.transactions,
                    acc.units,
                    round(acc.revenue),
                    lastPurchaseStr,
                    daysSince
            ));
        }

        customerRows.sort((a, b) -> Double.compare(b.getRevenue(), a.getRevenue()));

        int repeatCustomers = (int) customerRows.stream().filter(c -> c.getTransactions() >= 2).count();
        int inactiveCustomers = (int) customerRows.stream().filter(c -> c.getDaysSincePurchase() != null && c.getDaysSincePurchase() >= 30).count();

        AnalyticsDto.CustomerAnalysis customerAnalysis = new AnalyticsDto.CustomerAnalysis();
        customerAnalysis.setTotalCustomers(customers.size());
        customerAnalysis.setCustomersWithSales(customerRows.size());
        customerAnalysis.setRepeatCustomers(repeatCustomers);
        customerAnalysis.setInactiveCustomers(inactiveCustomers);
        customerAnalysis.setTopCustomers(customerRows.size() > 10 ? customerRows.subList(0, 10) : customerRows);

        AnalyticsDto.OverviewResponse response = new AnalyticsDto.OverviewResponse();
        response.setCompanyId(company.getId());
        response.setCompany(company.getName());
        response.setGeneratedAt(now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        response.setSalesTrends(salesTrends);
        response.setTopProducts(topProducts);
        response.setRevenueAnalysis(revenueAnalysis);
        response.setCustomerAnalysis(customerAnalysis);
        response.setProductProfitability(profitability);

        return response;
    }

    private static double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private static class CustomerAccumulator {
        int transactions = 0;
        int units = 0;
        double revenue = 0.0;
        OffsetDateTime lastPurchase = null;
    }
}
