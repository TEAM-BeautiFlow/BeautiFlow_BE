package com.beautiflow.global.common.config;

import com.beautiflow.global.common.security.CustomAuthenticationEntryPoint;
import com.beautiflow.global.common.security.authentication.CustomOAuth2UserService;
import com.beautiflow.global.common.security.authentication.CustomSuccessHandler;
import com.beautiflow.global.common.security.filter.JWTExceptionFilter;
import com.beautiflow.global.common.security.filter.JWTFilter;
import com.beautiflow.global.common.util.JWTUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JWTUtil jwtUtil;
    public final JWTExceptionFilter jwtExceptionFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

//    @Bean
//    public JWTFilter jwtFilter() {
//        return new JWTFilter(jwtUtil);
//    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .cors(corsCustomizer -> corsCustomizer.configurationSource(
                request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(
                            List.of("http://localhost:3000","http://localhost:5173","https://www.beautiflow.co.kr","http://localhost:8080", "https://beautiflow.co.kr"));
                    configuration.setAllowedMethods(
                            Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                    configuration.setAllowCredentials(true);
                    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
                    configuration.setMaxAge(3600L);
                    configuration.setExposedHeaders(List.of("Authorization"));
                    return configuration;
                }))

            .requestCache(cache -> cache.requestCache(new NullRequestCache()))
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(httpBasic -> httpBasic.disable())

            .oauth2Login((oauth2) -> oauth2
                .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                        .userService(customOAuth2UserService))
                .successHandler(customSuccessHandler))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/users/login",
                    "/connect/**",
                    "/users/auth/phone/send-code",
                    "/users/auth/phone/verify-code",
                    "/login/oauth2/code/kakao-customer",
                    "/login/oauth2/code/kakao-staff",
                    "/users/signup",
                    "/users/refresh",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/login/oauth2/**",
                    "/oauth2/**",
                    "/health",
                    "/shops/{shopId}",
                    "/shops/{shopId}/treatments",
                    "/shops/{shopId}/notices"
                ).permitAll()
                .anyRequest().authenticated())

            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(new JWTFilter(jwtUtil),
                UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtExceptionFilter, JWTFilter.class)
            .exceptionHandling(e -> e
                .authenticationEntryPoint(authenticationEntryPoint)
            );
        return http.build();
    }
}