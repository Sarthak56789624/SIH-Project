package com.aichainid.approval.entity;

import com.aichainid.access.entity.AccessRequest;
import com.aichainid.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "approvals", indexes = {
        @Index(name = "idx_approval_request", columnList = "access_request_id"),
        @Index(name = "idx_approval_admin", columnList = "admin_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "access_request_id", nullable = false)
    private AccessRequest accessRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalDecision decision;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "approved_at", nullable = false)
    private LocalDateTime approvedAt;

    @Column(name = "is_break_glass")
    @Builder.Default
    private Boolean isBreakGlass = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
