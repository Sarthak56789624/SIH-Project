package com.aichainid.asset.dto;

import com.aichainid.asset.entity.AssetAssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetAssignmentResponse {

    private Long id;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private Long userId;
    private String userEmail;
    private String userName;
    private Long assignedById;
    private String assignedByName;
    private LocalDateTime assignedAt;
    private LocalDateTime returnedAt;
    private AssetAssignmentStatus status;
    private String notes;
}
