package com.aichainid.identity.service;

import com.aichainid.identity.dto.IdentityCreateRequest;
import com.aichainid.identity.dto.IdentityResponse;
import com.aichainid.identity.dto.IdentityStatusUpdateRequest;
import com.aichainid.identity.dto.IdentityVerificationResponse;

public interface IdentityService {

    IdentityResponse createIdentity(IdentityCreateRequest request);

    IdentityResponse getIdentityByUserId(Long userId);

    IdentityResponse getIdentityByDid(String did);

    IdentityVerificationResponse verifyIdentity(Long id);

    IdentityResponse updateIdentityStatus(Long id, IdentityStatusUpdateRequest request);
}
