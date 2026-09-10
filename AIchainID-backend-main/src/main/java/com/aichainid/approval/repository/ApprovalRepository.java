package com.aichainid.approval.repository;

import com.aichainid.approval.entity.Approval;
import com.aichainid.approval.entity.ApprovalDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    List<Approval> findByAccessRequestId(Long accessRequestId);
    Optional<Approval> findTopByAccessRequestIdOrderByCreatedAtDesc(Long accessRequestId);
    List<Approval> findByAdminId(Long adminId);
    List<Approval> findByDecision(ApprovalDecision decision);
}
