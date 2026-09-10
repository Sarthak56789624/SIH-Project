package com.aichainid.organization.repository;

import com.aichainid.organization.entity.Organization;
import com.aichainid.organization.entity.OrganizationStatus;
import com.aichainid.organization.entity.OrganizationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByName(String name);
    boolean existsByName(String name);
    boolean existsByEmail(String email);
    List<Organization> findByStatus(OrganizationStatus status);
    List<Organization> findByOrganizationType(OrganizationType organizationType);
}
