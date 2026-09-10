package com.aichainid.risk.service;

import com.aichainid.access.entity.AccessRequest;
import com.aichainid.risk.dto.RiskAssessmentResponse;
import com.aichainid.risk.entity.RiskAssessment;

public interface RiskEngineService {

    RiskAssessment evaluateRisk(AccessRequest accessRequest);

    RiskAssessmentResponse evaluateAndSaveRisk(Long accessRequestId);
}
