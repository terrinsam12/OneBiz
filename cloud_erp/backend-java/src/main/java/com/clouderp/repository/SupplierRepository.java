package com.clouderp.repository;

import com.clouderp.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByCompanyId(Long companyId);
    List<Supplier> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
    Optional<Supplier> findByIdAndCompanyId(Long id, Long companyId);
    boolean existsByCompanyIdAndNameIgnoreCase(Long companyId, String name);
}
