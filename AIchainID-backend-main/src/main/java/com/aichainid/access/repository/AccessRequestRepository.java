package com.aichainid.access.repository;

import com.aichainid.access.entity.AccessRequest;
import com.aichainid.access.entity.AccessRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccessRequestRepository extends JpaRepository<AccessRequest, Long> {
    List<AccessRequest> findByRequesterId(Long requesterId);
    List<AccessRequest> findByResourceId(Long resourceId);
    List<AccessRequest> findByStatus(AccessRequestStatus status);
    List<AccessRequest> findByRequesterIdAndStatus(Long requesterId, AccessRequestStatus status);
    List<AccessRequest> findByRequesterIdAndResourceIdAndStatus(Long requesterId, Long resourceId, AccessRequestStatus status);
    long countByRequesterIdAndCreatedAtAfter(Long requesterId, LocalDateTime after);
}
