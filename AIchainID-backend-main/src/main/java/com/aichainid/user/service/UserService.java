package com.aichainid.user.service;

import com.aichainid.access.dto.AccessRequestResponse;
import com.aichainid.asset.dto.AssetAssignmentResponse;
import com.aichainid.credential.dto.CredentialResponse;
import com.aichainid.user.dto.UserCreateRequest;
import com.aichainid.user.dto.UserResponse;
import com.aichainid.user.dto.UserUpdateRequest;

import java.util.List;
import java.util.Set;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    Set<String> getUserRoles(Long id);

    List<CredentialResponse> getUserCredentials(Long id);

    List<AssetAssignmentResponse> getUserAssets(Long id);

    List<AccessRequestResponse> getUserAccessRequests(Long id);
}
