package com.clouderp.repository;

import com.clouderp.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    List<Notification> findByCompanyIdAndIsReadOrderByCreatedAtDesc(Long companyId, Boolean isRead);

    List<Notification> findByCompanyIdAndTypeOrderByCreatedAtDesc(Long companyId, String type);

    long countByCompanyIdAndIsReadFalse(Long companyId);

    Optional<Notification> findByIdAndCompanyId(Long id, Long companyId);

    Optional<Notification> findByCompanyIdAndDedupKeyAndIsReadFalse(Long companyId, String dedupKey);

    boolean existsByCompanyIdAndDedupKey(Long companyId, String dedupKey);

    boolean existsByCompanyIdAndDedupKeyAndIsReadFalse(Long companyId, String dedupKey);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now WHERE n.companyId = :companyId AND n.isRead = false")
    int markAllAsReadForCompany(@Param("companyId") Long companyId, @Param("now") OffsetDateTime now);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.companyId = :companyId AND n.isRead = true")
    int deleteReadForCompany(@Param("companyId") Long companyId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now WHERE n.companyId = :companyId AND n.dedupKey = :dedupKey AND n.isRead = false")
    int markDedupKeyAsReadForCompany(@Param("companyId") Long companyId, @Param("dedupKey") String dedupKey, @Param("now") OffsetDateTime now);
}
