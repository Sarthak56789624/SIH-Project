package com.aichainid.risk.repository;

import com.aichainid.risk.entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
    Optional<RiskAssessment> findByAccessRequestId(Long accessRequestId);
    boolean existsByAccessRequestId(Long accessRequestId);
}
