package com.aichainid.asset.repository;

import com.aichainid.asset.entity.Asset;
import com.aichainid.asset.entity.AssetStatus;
import com.aichainid.asset.entity.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByAssetCode(String assetCode);
    boolean existsByAssetCode(String assetCode);
    List<Asset> findByOrganizationId(Long organizationId);
    List<Asset> findByStatus(AssetStatus status);
    List<Asset> findByAssetType(AssetType assetType);
    List<Asset> findByOrganizationIdAndStatus(Long organizationId, AssetStatus status);
}
