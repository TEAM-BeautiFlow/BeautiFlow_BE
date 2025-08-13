package com.beautiflow.global.common.security.filter;

import com.beautiflow.global.common.util.JWTUtil;
import io.jsonwebtoken.JwtException; // 💡 import 추가
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        if (isPublicPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {
            if (jwtUtil.validateToken(token)) {
                Authentication authentication = jwtUtil.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException e) {
        }


        //세션에 사용자 등록
        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String requestURI) {
        List<String> publicPaths = Arrays.asList(
            "/",
            "/login",
            "/users/signup",
            "/users/refresh",
            "/swagger-ui/**", //** 패턴을 사용하기 위해 AntPathMatcher 필요
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/health",
            "/login/oauth2/**",
            "/oauth2/**"
        );

        return publicPaths.stream().anyMatch(path -> pathMatcher.match(path, requestURI));
    }
}