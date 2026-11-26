package com.clouderp.repository;

import com.clouderp.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByCompanyId(Long companyId);
    List<Expense> findByCompanyIdOrderByExpenseDateDescCreatedAtDesc(Long companyId);
    Optional<Expense> findByIdAndCompanyId(Long id, Long companyId);
    List<Expense> findByCompanyIdAndCategory(Long companyId, String category);
    List<Expense> findByCompanyIdAndExpenseDateBetween(Long companyId, LocalDate startDate, LocalDate endDate);
    long countByCompanyId(Long companyId);
}
