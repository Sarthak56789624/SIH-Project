package com.aichainid.identity.entity;

import com.aichainid.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "identities", indexes = {
        @Index(name = "idx_identity_did", columnList = "did")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Identity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true)
    private String did;

    @Column(name = "public_key", columnDefinition = "TEXT", nullable = false)
    private String publicKey;

    @Column(name = "did_method", nullable = false)
    @Builder.Default
    private String didMethod = "did:chainid";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private IdentityStatus status = IdentityStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
