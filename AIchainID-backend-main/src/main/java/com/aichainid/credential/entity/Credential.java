package com.aichainid.credential.entity;

import com.aichainid.organization.entity.Organization;
import com.aichainid.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "credentials", indexes = {
        @Index(name = "idx_credential_id", columnList = "credential_id"),
        @Index(name = "idx_credential_user", columnList = "user_id"),
        @Index(name = "idx_credential_issuer", columnList = "issuer_organization_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credential_id", nullable = false, unique = true)
    private String credentialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuer_organization_id", nullable = false)
    private Organization issuerOrganization;

    @Enumerated(EnumType.STRING)
    @Column(name = "credential_type", nullable = false)
    private CredentialType credentialType;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "credential_hash", nullable = false)
    private String credentialHash;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CredentialStatus status = CredentialStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
