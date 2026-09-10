package com.aichainid.permission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {

    private Long id;
    private String name;
    private String description;
    private String resourceType;
    private String action;
    private LocalDateTime createdAt;
}
