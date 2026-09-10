package com.aichainid.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRiskRequest {
    private String did;
    private String role;
    private String department;
    private String resource;
    private String time;           // "HH:MM" 24h
    private boolean deviceKnown;
    private boolean locationMatch;
    private int failedAttempts;
    private double requestFrequency;
    private boolean credentialValid;
    private double previousBehaviorScore;
}
