package com.clouderp.repository;

import com.clouderp.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCompanyId(Long companyId);
    List<Payment> findByCompanyIdOrderByPaymentDateDescCreatedAtDesc(Long companyId);
    List<Payment> findByCompanyIdAndSaleIdOrderByPaymentDateDesc(Long companyId, Long saleId);
    List<Payment> findByCompanyIdAndCustomerIdOrderByPaymentDateDesc(Long companyId, Long customerId);
    Optional<Payment> findByIdAndCompanyId(Long id, Long companyId);
    long countByCompanyId(Long companyId);
}
