package com.clouderp.repository;

import com.clouderp.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByCompanyId(Long companyId);
    Optional<Customer> findByIdAndCompanyId(Long id, Long companyId);
}
