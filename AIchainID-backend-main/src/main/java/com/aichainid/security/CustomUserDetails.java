package com.aichainid.security;

import com.aichainid.user.entity.User;
import com.aichainid.user.entity.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final Long organizationId;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.organizationId = user.getOrganization() != null ? user.getOrganization().getId() : null;
        this.enabled = user.getStatus() == UserStatus.ACTIVE;

        Set<GrantedAuthority> auths = new HashSet<>();
        if (user.getRoles() != null) {
            user.getRoles().forEach(role -> {
                // Add ROLE_ prefix if not present
                String roleName = role.getName().startsWith("ROLE_") ? role.getName() : "ROLE_" + role.getName();
                auths.add(new SimpleGrantedAuthority(roleName));

                // Add individual permissions
                if (role.getPermissions() != null) {
                    role.getPermissions().forEach(permission ->
                            auths.add(new SimpleGrantedAuthority(permission.getName())));
                }
            });
        }
        this.authorities = auths;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
