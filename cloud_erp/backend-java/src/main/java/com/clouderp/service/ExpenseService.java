package com.clouderp.service;

import com.clouderp.config.SecurityUtils;
import com.clouderp.dto.ExpenseDto;
import com.clouderp.exception.ResourceNotFoundException;
import com.clouderp.model.Expense;
import com.clouderp.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional(readOnly = true)
    public List<ExpenseDto.Response> getExpenses(String search, String category, LocalDate startDate, LocalDate endDate) {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        List<Expense> expenses = expenseRepository.findByCompanyIdOrderByExpenseDateDescCreatedAtDesc(companyId);

        return expenses.stream()
                .filter(e -> {
                    if (StringUtils.hasText(category) && !"ALL".equalsIgnoreCase(category)) {
                        if (!category.equalsIgnoreCase(e.getCategory())) {
                            return false;
                        }
                    }
                    if (startDate != null && e.getExpenseDate().isBefore(startDate)) {
                        return false;
                    }
                    if (endDate != null && e.getExpenseDate().isAfter(endDate)) {
                        return false;
                    }
                    if (StringUtils.hasText(search)) {
                        String q = search.trim().toLowerCase();
                        boolean matchTitle = e.getTitle() != null && e.getTitle().toLowerCase().contains(q);
                        boolean matchDesc = e.getDescription() != null && e.getDescription().toLowerCase().contains(q);
                        boolean matchMethod = e.getPaymentMethod() != null && e.getPaymentMethod().toLowerCase().contains(q);
                        boolean matchCat = e.getCategory() != null && e.getCategory().toLowerCase().contains(q);
                        if (!matchTitle && !matchDesc && !matchMethod && !matchCat) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(ExpenseDto.Response::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExpenseDto.Response getExpenseById(Long id) {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        Expense expense = expenseRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        return ExpenseDto.Response.fromEntity(expense);
    }

    public ExpenseDto.Response createExpense(ExpenseDto.Create request) {
        SecurityUtils.requireAnyRole("COMPANY_ADMIN", "MANAGER");
        Long companyId = SecurityUtils.requireCurrentCompanyId();

        Expense expense = new Expense(
                companyId,
                request.getTitle().trim(),
                request.getCategory().trim(),
                request.getAmount(),
                request.getExpenseDate() != null ? request.getExpenseDate() : LocalDate.now(),
                request.getPaymentMethod() != null ? request.getPaymentMethod().trim() : "Cash",
                request.getDescription() != null ? request.getDescription().trim() : "",
                request.getReceiptUrl(),
                request.getReceiptName()
        );

        Expense saved = expenseRepository.save(expense);
        return ExpenseDto.Response.fromEntity(saved);
    }

    public ExpenseDto.Response updateExpense(Long id, ExpenseDto.Update request) {
        SecurityUtils.requireAnyRole("COMPANY_ADMIN", "MANAGER");
        Long companyId = SecurityUtils.requireCurrentCompanyId();

        Expense expense = expenseRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        expense.setTitle(request.getTitle().trim());
        expense.setCategory(request.getCategory().trim());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate() != null ? request.getExpenseDate() : expense.getExpenseDate());
        expense.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod().trim() : "Cash");
        expense.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");

        if (request.getReceiptUrl() != null) {
            expense.setReceiptUrl(request.getReceiptUrl());
            expense.setReceiptName(request.getReceiptName());
        }

        Expense updated = expenseRepository.save(expense);
        return ExpenseDto.Response.fromEntity(updated);
    }

    public void deleteExpense(Long id) {
        SecurityUtils.requireAnyRole("COMPANY_ADMIN", "MANAGER");
        Long companyId = SecurityUtils.requireCurrentCompanyId();

        Expense expense = expenseRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public ExpenseDto.SummaryResponse getSummary() {
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        List<Expense> expenses = expenseRepository.findByCompanyId(companyId);

        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();
        long expenseCount = expenses.size();

        YearMonth currentYm = YearMonth.now();
        YearMonth lastYm = currentYm.minusMonths(1);

        double thisMonthExpenses = expenses.stream()
                .filter(e -> YearMonth.from(e.getExpenseDate()).equals(currentYm))
                .mapToDouble(Expense::getAmount)
                .sum();

        double lastMonthExpenses = expenses.stream()
                .filter(e -> YearMonth.from(e.getExpenseDate()).equals(lastYm))
                .mapToDouble(Expense::getAmount)
                .sum();

        // Category breakdown
        Map<String, List<Expense>> byCategory = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory));

        List<ExpenseDto.CategorySummary> categoryBreakdown = new ArrayList<>();
        String topCategory = "None";
        double topCategoryAmount = 0.0;

        for (Map.Entry<String, List<Expense>> entry : byCategory.entrySet()) {
            double catTotal = entry.getValue().stream().mapToDouble(Expense::getAmount).sum();
            long count = entry.getValue().size();
            double pct = totalExpenses > 0 ? (catTotal / totalExpenses) * 100.0 : 0.0;

            categoryBreakdown.add(new ExpenseDto.CategorySummary(entry.getKey(), catTotal, count, Math.round(pct * 10.0) / 10.0));

            if (catTotal > topCategoryAmount) {
                topCategoryAmount = catTotal;
                topCategory = entry.getKey();
            }
        }
        categoryBreakdown.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));

        // Monthly trends for last 6 months
        List<ExpenseDto.MonthlyTrend> monthlyTrends = new ArrayList<>();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = currentYm.minusMonths(i);
            String monthKey = ym.toString();
            String label = ym.format(labelFormatter);

            List<Expense> monthExpenses = expenses.stream()
                    .filter(e -> YearMonth.from(e.getExpenseDate()).equals(ym))
                    .collect(Collectors.toList());

            double monthAmount = monthExpenses.stream().mapToDouble(Expense::getAmount).sum();
            long count = monthExpenses.size();

            monthlyTrends.add(new ExpenseDto.MonthlyTrend(monthKey, label, monthAmount, count));
        }

        return new ExpenseDto.SummaryResponse(
                totalExpenses,
                thisMonthExpenses,
                lastMonthExpenses,
                expenseCount,
                topCategory,
                topCategoryAmount,
                categoryBreakdown,
                monthlyTrends
        );
    }
}
