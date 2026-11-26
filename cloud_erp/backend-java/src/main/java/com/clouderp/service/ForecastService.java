package com.clouderp.service;

import com.clouderp.config.SecurityUtils;
import com.clouderp.dto.ForecastDto;
import com.clouderp.model.Company;
import com.clouderp.model.Product;
import com.clouderp.model.Sale;
import com.clouderp.repository.ProductRepository;
import com.clouderp.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ForecastService {

    private final CompanyService companyService;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;

    private static final DateTimeFormatter MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    public ForecastService(CompanyService companyService,
                           ProductRepository productRepository,
                           SaleRepository saleRepository) {
        this.companyService = companyService;
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
    }

    public ForecastDto.Response getForecast(Long requestedCompanyId) {
        // Enforce company ID from authenticated user
        Long companyId = SecurityUtils.getCurrentCompanyId();
        Company company = companyService.getCompanyById(companyId);

        List<Product> products = productRepository.findByCompanyId(companyId);
        List<Sale> sales = saleRepository.findByCompanyIdOrderByCreatedAtAsc(companyId);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        YearMonth currentYm = YearMonth.from(now);

        // Build 6 calendar history months
        List<YearMonth> historyMonthList = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            historyMonthList.add(currentYm.minusMonths(i));
        }

        List<String> monthKeys = historyMonthList.stream()
                .map(YearMonth::toString)
                .collect(Collectors.toList());

        // Build 3 future forecast months
        List<YearMonth> forecastMonthList = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            forecastMonthList.add(currentYm.plusMonths(i));
        }

        // Aggregate demand: product_id -> month_key -> units sold
        Map<Long, Map<String, Integer>> demand = new HashMap<>();
        for (Product product : products) {
            Map<String, Integer> map = new LinkedHashMap<>();
            for (String key : monthKeys) {
                map.put(key, 0);
            }
            demand.put(product.getId(), map);
        }

        for (Sale sale : sales) {
            if (sale.getCreatedAt() == null) continue;
            Map<String, Integer> prodDemand = demand.get(sale.getProductId());
            if (prodDemand == null) continue;

            String key = YearMonth.from(sale.getCreatedAt()).toString();
            if (prodDemand.containsKey(key)) {
                prodDemand.put(key, prodDemand.get(key) + (sale.getQuantity() != null ? sale.getQuantity() : 0));
            }
        }

        List<ForecastDto.ProductForecast> forecastRows = new ArrayList<>();
        int totalForecastUnits = 0;

        for (Product product : products) {
            List<Integer> history = new ArrayList<>();
            Map<String, Integer> prodDemand = demand.get(product.getId());
            int historySum = 0;
            for (String key : monthKeys) {
                int val = prodDemand != null ? prodDemand.getOrDefault(key, 0) : 0;
                history.add(val);
                historySum += val;
            }

            ForecastDto.ProductForecast row = new ForecastDto.ProductForecast();
            row.setProductId(product.getId());
            row.setProduct(product.getName());
            row.setCurrentStock(product.getQuantity());
            row.setReorderLevel(product.getReorderLevel());
            row.setHistoricalUnits(history);

            if (historySum == 0) {
                row.setForecastMethod("No history");
                List<ForecastDto.MonthlyForecastItem> monthlyForecast = forecastMonthList.stream()
                        .map(ym -> new ForecastDto.MonthlyForecastItem(ym.toString(), ym.format(MONTH_LABEL_FORMATTER), 0))
                        .collect(Collectors.toList());
                row.setForecast(monthlyForecast);
                row.setNextMonthDemand(0);
                row.setNextThreeMonthDemand(0);
                row.setStockAfterNextMonth(product.getQuantity());
                row.setStockoutRisk("UNKNOWN");
                row.setRecommendedReorder(0);
                forecastRows.add(row);
                continue;
            }

            PredictionResult pred = predict(history);
            int predictedUnits = (int) Math.round(pred.value);

            List<ForecastDto.MonthlyForecastItem> monthlyForecast = forecastMonthList.stream()
                    .map(ym -> new ForecastDto.MonthlyForecastItem(ym.toString(), ym.format(MONTH_LABEL_FORMATTER), predictedUnits))
                    .collect(Collectors.toList());

            int nextMonthDemand = predictedUnits;
            int nextThreeMonthDemand = predictedUnits * 3;
            int stockAfterNextMonth = Math.max(0, product.getQuantity() - nextMonthDemand);

            int targetStock = nextThreeMonthDemand + product.getReorderLevel();
            int recommendedReorder = Math.max(0, targetStock - product.getQuantity());

            String stockoutRisk;
            if (product.getQuantity() <= product.getReorderLevel()) {
                stockoutRisk = "HIGH";
            } else if (product.getQuantity() <= nextMonthDemand) {
                stockoutRisk = "HIGH";
            } else if (product.getQuantity() <= nextMonthDemand * 2) {
                stockoutRisk = "MEDIUM";
            } else {
                stockoutRisk = "LOW";
            }

            totalForecastUnits += nextThreeMonthDemand;

            row.setForecastMethod(pred.method);
            row.setForecast(monthlyForecast);
            row.setNextMonthDemand(nextMonthDemand);
            row.setNextThreeMonthDemand(nextThreeMonthDemand);
            row.setStockAfterNextMonth(stockAfterNextMonth);
            row.setStockoutRisk(stockoutRisk);
            row.setRecommendedReorder(recommendedReorder);

            forecastRows.add(row);
        }

        // Sorting: risk order (HIGH=0, MEDIUM=1, LOW=2, UNKNOWN=3), then recommendedReorder desc, then product name asc
        Map<String, Integer> riskOrder = Map.of("HIGH", 0, "MEDIUM", 1, "LOW", 2, "UNKNOWN", 3);
        forecastRows.sort((a, b) -> {
            int rA = riskOrder.getOrDefault(a.getStockoutRisk(), 3);
            int rB = riskOrder.getOrDefault(b.getStockoutRisk(), 3);
            if (rA != rB) return Integer.compare(rA, rB);
            int reorderDiff = Integer.compare(b.getRecommendedReorder(), a.getRecommendedReorder());
            if (reorderDiff != 0) return reorderDiff;
            return a.getProduct().compareToIgnoreCase(b.getProduct());
        });

        int highRisk = (int) forecastRows.stream().filter(r -> "HIGH".equals(r.getStockoutRisk())).count();
        int mediumRisk = (int) forecastRows.stream().filter(r -> "MEDIUM".equals(r.getStockoutRisk())).count();

        List<ForecastDto.MonthLabel> historyLabels = historyMonthList.stream()
                .map(ym -> new ForecastDto.MonthLabel(ym.toString(), ym.format(MONTH_LABEL_FORMATTER)))
                .collect(Collectors.toList());

        List<ForecastDto.MonthLabel> forecastLabels = forecastMonthList.stream()
                .map(ym -> new ForecastDto.MonthLabel(ym.toString(), ym.format(MONTH_LABEL_FORMATTER)))
                .collect(Collectors.toList());

        ForecastDto.Summary summary = new ForecastDto.Summary(
                products.size(),
                highRisk,
                mediumRisk,
                totalForecastUnits
        );

        ForecastDto.Response response = new ForecastDto.Response();
        response.setCompanyId(company.getId());
        response.setCompany(company.getName());
        response.setGeneratedAt(now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        response.setHistoryMonths(historyLabels);
        response.setForecastMonths(forecastLabels);
        response.setSummary(summary);
        response.setForecasts(forecastRows);

        return response;
    }

    private PredictionResult predict(List<Integer> values) {
        List<Double> positiveValues = values.stream()
                .map(v -> Math.max(0.0, v.doubleValue()))
                .collect(Collectors.toList());

        if (positiveValues.isEmpty()) {
            return new PredictionResult(0.0, "No history");
        }

        long nonZeroCount = positiveValues.stream().filter(v -> v > 0).count();

        if (nonZeroCount < 2) {
            int fromIdx = Math.max(0, positiveValues.size() - 3);
            List<Double> recent = positiveValues.subList(fromIdx, positiveValues.size());
            double avg = recent.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            return new PredictionResult(Math.max(0.0, avg), "Recent average");
        }

        int n = positiveValues.size();
        double xMean = (n - 1) / 2.0;
        double yMean = positiveValues.stream().mapToDouble(Double::doubleValue).sum() / n;

        double denominator = 0.0;
        for (int i = 0; i < n; i++) {
            denominator += Math.pow(i - xMean, 2);
        }

        if (denominator == 0.0) {
            return new PredictionResult(yMean, "Average");
        }

        double slope = 0.0;
        for (int i = 0; i < n; i++) {
            slope += (i - xMean) * (positiveValues.get(i) - yMean);
        }
        slope /= denominator;

        double intercept = yMean - slope * xMean;
        double futureIndex = n;
        double prediction = intercept + slope * futureIndex;

        return new PredictionResult(Math.max(0.0, prediction), "Linear trend");
    }

    private static class PredictionResult {
        final double value;
        final String method;

        PredictionResult(double value, String method) {
            this.value = value;
            this.method = method;
        }
    }
}
