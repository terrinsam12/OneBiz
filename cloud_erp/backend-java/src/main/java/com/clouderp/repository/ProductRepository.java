package com.clouderp.repository;

import com.clouderp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCompanyId(Long companyId);
    Optional<Product> findByIdAndCompanyId(Long id, Long companyId);
}
