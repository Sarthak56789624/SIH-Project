package com.aichainid.access.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessRequestCreateRequest {

    @NotNull(message = "Resource ID is required")
    private Long resourceId;

    @NotBlank(message = "Reason for access is required")
    private String reason;

    @NotNull(message = "Requested start time is required")
    private LocalDateTime requestedFrom;

    @NotNull(message = "Requested end time is required")
    @Future(message = "Requested until time must be in the future")
    private LocalDateTime requestedUntil;
}
