package com.aichainid.credential.repository;

import com.aichainid.credential.entity.Credential;
import com.aichainid.credential.entity.CredentialStatus;
import com.aichainid.credential.entity.CredentialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {
    Optional<Credential> findByCredentialId(String credentialId);
    boolean existsByCredentialId(String credentialId);
    List<Credential> findByUserId(Long userId);
    List<Credential> findByUserIdAndStatus(Long userId, CredentialStatus status);
    List<Credential> findByIssuerOrganizationId(Long organizationId);
    List<Credential> findByCredentialType(CredentialType credentialType);
}
