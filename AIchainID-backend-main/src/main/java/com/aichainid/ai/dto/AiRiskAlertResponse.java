package com.aichainid.ai.dto;

import java.util.List;

/**
 * Maps the Python /api/risk-alerts response:
 * {"alerts": [...], "total": int}
 */
public class AiRiskAlertResponse {

    private List<Alert> alerts;
    private int total;

    public AiRiskAlertResponse() {}
    public AiRiskAlertResponse(List<Alert> alerts, int total) {
        this.alerts = alerts;
        this.total = total;
    }

    public List<Alert> getAlerts() { return alerts; }
    public void setAlerts(List<Alert> alerts) { this.alerts = alerts; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public static class Alert {
        private String id;
        private String employeeDid;
        private String employeeName;
        private double riskScore;
        private String riskLevel;    // LOW | MEDIUM | HIGH
        private String resource;
        private List<String> reasons;
        private String decision;     // ALLOW | ADMIN_REVIEW | BLOCK
        private String status;       // active | resolved
        private String createdAt;

        public Alert() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getEmployeeDid() { return employeeDid; }
        public void setEmployeeDid(String employeeDid) { this.employeeDid = employeeDid; }
        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
        public double getRiskScore() { return riskScore; }
        public void setRiskScore(double riskScore) { this.riskScore = riskScore; }
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
        public String getResource() { return resource; }
        public void setResource(String resource) { this.resource = resource; }
        public List<String> getReasons() { return reasons; }
        public void setReasons(List<String> reasons) { this.reasons = reasons; }
        public String getDecision() { return decision; }
        public void setDecision(String decision) { this.decision = decision; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}
