package com.aichainid.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPermissionRequest {
    private String role;
    private String department;
    private String employment_type;
    private List<String> credentials;
    private List<String> projects;
    private List<String> resources;
    private int contract_duration_days;
    private boolean previously_approved_similar;
}
