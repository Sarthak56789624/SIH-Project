package com.aichainid.organization.dto;

import com.aichainid.organization.entity.OrganizationStatus;
import com.aichainid.organization.entity.OrganizationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {

    private Long id;
    private String name;
    private String description;
    private OrganizationType organizationType;
    private String email;
    private String phone;
    private String address;
    private OrganizationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
