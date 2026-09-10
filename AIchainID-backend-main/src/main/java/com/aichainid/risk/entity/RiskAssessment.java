package com.aichainid.risk.entity;

import com.aichainid.access.entity.AccessRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "risk_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "access_request_id", nullable = false, unique = true)
    private AccessRequest accessRequest;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskRecommendation recommendation;

    @Column(name = "risk_factors", columnDefinition = "TEXT")
    private String riskFactors;

    @Column(name = "model_version", nullable = false)
    @Builder.Default
    private String modelVersion = "v1.0-rule-engine";

    @CreationTimestamp
    @Column(name = "analyzed_at", nullable = false, updatable = false)
    private LocalDateTime analyzedAt;
}
