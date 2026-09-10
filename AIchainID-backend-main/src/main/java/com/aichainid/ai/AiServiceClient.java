package com.aichainid.ai;

import com.aichainid.ai.dto.AiPermissionRequest;
import com.aichainid.ai.dto.AiPermissionResponse;
import com.aichainid.ai.dto.AiRiskAlertResponse;
import com.aichainid.ai.dto.AiRiskRequest;
import com.aichainid.ai.dto.AiRiskResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client that calls the Python FastAPI AI microservice.
 * All frontend AI calls go through this bridge (Java → Python).
 */
@Service
public class AiServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AiServiceClient.class);

    private final RestTemplate restTemplate;
    private final String aiBaseUrl;

    public AiServiceClient(@Value("${ai.service.url:http://localhost:8000}") String aiBaseUrl) {
        this.aiBaseUrl = aiBaseUrl;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Calls POST /ai/recommend-permissions on the Python service.
     */
    public AiPermissionResponse getPermissionRecommendation(AiPermissionRequest request) {
        String url = aiBaseUrl + "/ai/recommend-permissions";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AiPermissionRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<AiPermissionResponse> response =
                    restTemplate.postForEntity(url, entity, AiPermissionResponse.class);

            log.info("AI permission recommendation received for role={} dept={} resources={}",
                    request.getRole(), request.getDepartment(), request.getResources());
            return response.getBody();

        } catch (ResourceAccessException e) {
            log.error("AI service unreachable at {}: {}", url, e.getMessage());
            throw new RuntimeException("AI service is currently unavailable. Please ensure the Python AI service is running on " + aiBaseUrl, e);
        } catch (Exception e) {
            log.error("Error calling AI permission service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get AI permission recommendation: " + e.getMessage(), e);
        }
    }

    /**
     * Calls POST /ai/risk-score on the Python service.
     */
    public AiRiskResponse getRiskScore(AiRiskRequest request) {
        String url = aiBaseUrl + "/ai/risk-score";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AiRiskRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<AiRiskResponse> response =
                    restTemplate.postForEntity(url, entity, AiRiskResponse.class);

            AiRiskResponse result = response.getBody();
            if (result != null) {
                log.info("AI risk score for did={} resource={}: score={} level={} decision={}",
                        request.getDid(), request.getResource(),
                        result.getRiskScore(), result.getRiskLevel(), result.getDecision());
            }
            return result;

        } catch (ResourceAccessException e) {
            log.error("AI service unreachable at {}: {}", url, e.getMessage());
            throw new RuntimeException("AI service is currently unavailable. Please ensure the Python AI service is running on " + aiBaseUrl, e);
        } catch (Exception e) {
            log.error("Error calling AI risk service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get AI risk score: " + e.getMessage(), e);
        }
    }

    /**
     * Calls GET /api/risk-alerts on the Python service.
     * Returns the full risk-alert envelope {alerts, total}.
     */
    public AiRiskAlertResponse getRiskAlerts() {
        String url = aiBaseUrl + "/api/risk-alerts";
        try {
            ResponseEntity<AiRiskAlertResponse> response =
                    restTemplate.getForEntity(url, AiRiskAlertResponse.class);
            AiRiskAlertResponse body = response.getBody();
            log.info("Fetched {} risk alerts from AI service", body != null ? body.getTotal() : 0);
            return body;
        } catch (ResourceAccessException e) {
            log.error("AI service unreachable at {}: {}", url, e.getMessage());
            throw new RuntimeException("AI service is currently unavailable. Please ensure the Python AI service is running on " + aiBaseUrl, e);
        } catch (Exception e) {
            log.error("Error fetching risk alerts from AI service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch risk alerts: " + e.getMessage(), e);
        }
    }

    /**
     * Health check — returns true if the Python AI service is reachable.
     */
    public boolean isAiServiceHealthy() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(aiBaseUrl + "/health", String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}
