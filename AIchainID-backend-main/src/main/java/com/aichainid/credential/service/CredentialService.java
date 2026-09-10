package com.aichainid.credential.service;

import com.aichainid.credential.dto.CredentialCreateRequest;
import com.aichainid.credential.dto.CredentialResponse;
import com.aichainid.credential.dto.CredentialUpdateRequest;
import com.aichainid.credential.dto.CredentialVerificationResponse;

import java.util.List;

public interface CredentialService {

    CredentialResponse issueCredential(CredentialCreateRequest request);

    List<CredentialResponse> getAllCredentials();

    CredentialResponse getCredentialById(Long id);

    CredentialResponse getCredentialByCredentialId(String credentialId);

    List<CredentialResponse> getCredentialsByUserId(Long userId);

    CredentialVerificationResponse verifyCredential(Long id);

    CredentialResponse revokeCredential(Long id, String reason);

    CredentialResponse updateCredential(Long id, CredentialUpdateRequest request);
}
