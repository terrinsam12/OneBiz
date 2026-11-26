package com.clouderp.repository;

import com.clouderp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    List<User> findByCompanyId(Long companyId);
    Optional<User> findByIdAndCompanyId(Long id, Long companyId);
    List<User> findByCompanyIdOrderByCreatedAtDesc(Long companyId);
    List<User> findByCompanyIdAndRole(Long companyId, String role);
}
