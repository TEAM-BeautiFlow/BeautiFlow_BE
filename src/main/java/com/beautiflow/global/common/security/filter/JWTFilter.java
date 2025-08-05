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

        // 💡 [수정] try-catch 문으로 감싸기
        try {
            if (jwtUtil.validateToken(token)) {
                Authentication authentication = jwtUtil.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException e) {
            // 유효하지 않은 토큰(만료, 형식 오류 등)의 예외를 여기서 처리합니다.
            // SecurityContext에 아무것도 설정하지 않고 넘어가면,
            // 뒤따르는 필터에서 '인증되지 않은 사용자'로 간주하여 정상적으로 401/403 오류를 반환합니다.
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
            "/health"
        );

        return publicPaths.stream().anyMatch(path -> pathMatcher.match(path, requestURI));
    }
}