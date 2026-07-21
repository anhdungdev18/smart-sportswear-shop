package com.dunghaiquyen.ecommerce.common.security;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
public class CustomUserDetails implements UserDetails {
    private final UUID userId;
    private final String role;
    public CustomUserDetails(UUID userId, String role) { this.userId = userId; this.role = role; }
    public UUID getUserId() { return userId; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(new SimpleGrantedAuthority("ROLE_" + role)); }
    @Override public String getPassword() { return ""; }
    @Override public String getUsername() { return userId.toString(); }
}