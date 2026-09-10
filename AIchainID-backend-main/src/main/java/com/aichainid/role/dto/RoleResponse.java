package com.aichainid.role.dto;

import com.aichainid.permission.dto.PermissionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    private Long id;
    private String name;
    private String description;
    private Long organizationId;
    private String organizationName;
    private Set<PermissionResponse> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
