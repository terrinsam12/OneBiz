package com.clouderp.repository;

import com.clouderp.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByCompanyId(Long companyId);
    List<Purchase> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
    List<Purchase> findByCompanyIdAndSupplierIdOrderByCreatedAtDesc(Long companyId, Long supplierId);
    List<Purchase> findByCompanyIdAndStatusOrderByCreatedAtDesc(Long companyId, String status);
    Optional<Purchase> findByIdAndCompanyId(Long id, Long companyId);
    long countByCompanyId(Long companyId);
    long countByCompanyIdAndSupplierId(Long companyId, Long supplierId);
}
