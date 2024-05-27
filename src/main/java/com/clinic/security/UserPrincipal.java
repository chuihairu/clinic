package com.clinic.security;

import com.clinic.entity.StaffEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
public class UserPrincipal implements UserDetails {
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    private boolean enabled;
    private List<GrantedAuthority> authorities;

    public UserPrincipal(StaffEntity staff) {
        this.username = staff.getUsername();
        this.password = staff.getPassword();
        this.enabled = staff.isEnabled();
        this.authorities = (List<GrantedAuthority>) staff.getAuthorities();
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
        return username;
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
