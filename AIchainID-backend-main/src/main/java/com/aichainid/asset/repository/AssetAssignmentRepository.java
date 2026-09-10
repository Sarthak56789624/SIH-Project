package com.aichainid.asset.repository;

import com.aichainid.asset.entity.AssetAssignment;
import com.aichainid.asset.entity.AssetAssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Long> {
    List<AssetAssignment> findByAssetId(Long assetId);
    List<AssetAssignment> findByUserId(Long userId);
    List<AssetAssignment> findByUserIdAndStatus(Long userId, AssetAssignmentStatus status);
    Optional<AssetAssignment> findByAssetIdAndStatus(Long assetId, AssetAssignmentStatus status);
    List<AssetAssignment> findByAssignedById(Long assignedById);
}
