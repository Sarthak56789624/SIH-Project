package com.aichainid.access.entity;

import com.aichainid.resource.entity.Resource;
import com.aichainid.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "access_requests", indexes = {
        @Index(name = "idx_access_requester", columnList = "requester_id"),
        @Index(name = "idx_access_resource", columnList = "resource_id"),
        @Index(name = "idx_access_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "requested_from", nullable = false)
    private LocalDateTime requestedFrom;

    @Column(name = "requested_until", nullable = false)
    private LocalDateTime requestedUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccessRequestStatus status = AccessRequestStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
