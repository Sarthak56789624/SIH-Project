package com.aichainid.identity.repository;

import com.aichainid.identity.entity.Identity;
import com.aichainid.identity.entity.IdentityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IdentityRepository extends JpaRepository<Identity, Long> {
    Optional<Identity> findByUserId(Long userId);
    Optional<Identity> findByDid(String did);
    boolean existsByUserId(Long userId);
    boolean existsByDid(String did);
    List<Identity> findByStatus(IdentityStatus status);
}
