package com.metaverse.planti_be.auth.dto;

import com.metaverse.planti_be.auth.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final UserEntity userEntity;

    public CustomUserDetails(UserEntity userEntity) {

        this.userEntity = userEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // UserRole enum 객체 자체를 리스트에 담아 반환
        return Collections.singletonList(userEntity.getRole());
    }

    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return userEntity.getName();
    }
    // 계정 만료 여부 (true = 만료되지 않음)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    // 계정 잠금 여부 (true = 잠금되지 않음)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    // 자격 증명(비밀번호) 만료 여부 (true = 만료되지 않음)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    // 계정 활성화 여부 (true = 활성화됨)
    @Override
    public boolean isEnabled() {
        return true;
    }
}