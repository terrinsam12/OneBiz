package com.clouderp.repository;

import com.clouderp.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
    List<Sale> findByCompanyIdOrderByCreatedAtAsc(Long companyId);
    List<Sale> findByCompanyId(Long companyId);
    Optional<Sale> findByIdAndCompanyId(Long id, Long companyId);
    List<Sale> findByCompanyIdAndCustomerId(Long companyId, Long customerId);
    long countByCompanyId(Long companyId);
}
