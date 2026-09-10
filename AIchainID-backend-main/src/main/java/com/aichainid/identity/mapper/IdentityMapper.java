package com.aichainid.identity.mapper;

import com.aichainid.identity.dto.IdentityResponse;
import com.aichainid.identity.entity.Identity;
import org.springframework.stereotype.Component;

@Component
public class IdentityMapper {

    public IdentityResponse toResponse(Identity identity) {
        if (identity == null) {
            return null;
        }
        return IdentityResponse.builder()
                .id(identity.getId())
                .userId(identity.getUser() != null ? identity.getUser().getId() : null)
                .userEmail(identity.getUser() != null ? identity.getUser().getEmail() : null)
                .userName(identity.getUser() != null ? identity.getUser().getFirstName() + " " + identity.getUser().getLastName() : null)
                .did(identity.getDid())
                .publicKey(identity.getPublicKey())
                .didMethod(identity.getDidMethod())
                .status(identity.getStatus())
                .createdAt(identity.getCreatedAt())
                .updatedAt(identity.getUpdatedAt())
                .build();
    }
}
