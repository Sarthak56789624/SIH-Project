package com.aichainid.resource.repository;

import com.aichainid.resource.entity.Resource;
import com.aichainid.resource.entity.ResourceSensitivityLevel;
import com.aichainid.resource.entity.ResourceStatus;
import com.aichainid.resource.entity.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByOrganizationId(Long organizationId);
    List<Resource> findByResourceType(ResourceType resourceType);
    List<Resource> findByStatus(ResourceStatus status);
    List<Resource> findBySensitivityLevel(ResourceSensitivityLevel sensitivityLevel);
}
