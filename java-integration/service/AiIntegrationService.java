package com.aichainid.ai.service;

import com.aichainid.ai.dto.AiPermissionDtos.AiPermissionRequest;
import com.aichainid.ai.dto.AiPermissionDtos.AiPermissionResponse;
import com.aichainid.ai.dto.AiRiskScoreDtos.AiRiskScoreRequest;
import com.aichainid.ai.dto.AiRiskScoreDtos.AiRiskScoreResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Thin HTTP client wrapping the two Python FastAPI AI endpoints:
 *   POST /ai/recommend-permissions
 *   POST /ai/risk-score
 *
 * Kept separate from RuleBasedRiskEngineService so you can choose, per call
 * site, whether to use the ML-enhanced Python service or the existing
 * pure-Java rule engine (e.g. as a fallback if the AI service is down).
 */
@Service
public class AiIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(AiIntegrationService.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final WebClient aiServiceWebClient;

    public AiIntegrationService(WebClient aiServiceWebClient) {
        this.aiServiceWebClient = aiServiceWebClient;
    }

    public AiRiskScoreResponse getRiskScore(AiRiskScoreRequest request) {
        try {
            return aiServiceWebClient.post()
                    .uri("/ai/risk-score")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AiRiskScoreResponse.class)
                    .timeout(TIMEOUT)
                    .block();
        } catch (Exception e) {
            log.error("AI risk-score service call failed, caller should fall back to RuleBasedRiskEngineService", e);
            throw new AiServiceUnavailableException("Risk scoring AI service unavailable", e);
        }
    }

    public AiPermissionResponse getPermissionRecommendations(AiPermissionRequest request) {
        try {
            return aiServiceWebClient.post()
                    .uri("/ai/recommend-permissions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AiPermissionResponse.class)
                    .timeout(TIMEOUT)
                    .block();
        } catch (Exception e) {
            log.error("AI permission-recommendation service call failed", e);
            throw new AiServiceUnavailableException("Permission recommendation AI service unavailable", e);
        }
    }

    public static class AiServiceUnavailableException extends RuntimeException {
        public AiServiceUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
