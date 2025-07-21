package com.beautiflow.global.common.security;


import com.beautiflow.global.domain.GlobalRole;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class CustomOAuth2User implements OAuth2User {

    @Getter
    private final String provider;
    @Getter
    private final String kakaoId;
    @Getter
    private final Long userId;
    private final GlobalRole role;


    public CustomOAuth2User(String provider, String kakaoId, Long userId, GlobalRole role) {
        this.kakaoId = kakaoId;
        this.provider = provider;
        this.userId = userId;
        this.role = role;
    }


    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getName() {
        return kakaoId;
    }


}