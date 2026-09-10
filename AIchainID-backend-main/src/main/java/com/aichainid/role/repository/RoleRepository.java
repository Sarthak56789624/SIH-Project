package com.aichainid.role.repository;

import com.aichainid.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    Optional<Role> findByNameAndOrganizationId(String name, Long organizationId);
    List<Role> findByOrganizationId(Long organizationId);
    boolean existsByNameAndOrganizationId(String name, Long organizationId);
    boolean existsByName(String name);
}
