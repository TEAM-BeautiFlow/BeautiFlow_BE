package com.beautiflow.global.common.config;

import com.beautiflow.global.common.security.CustomAuthenticationEntryPoint;
import com.beautiflow.global.common.security.authentication.CustomOAuth2UserService;
import com.beautiflow.global.common.security.authentication.CustomSuccessHandler;
import com.beautiflow.global.common.security.filter.JWTExceptionFilter;
import com.beautiflow.global.common.security.filter.JWTFilter;
import com.beautiflow.global.common.util.JWTUtil;
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

    @Bean
    public JWTFilter jwtFilter() {
        return new JWTFilter(jwtUtil);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http

            .cors(corsCustomizer -> corsCustomizer.configurationSource(
                request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(
                        List.of("http://localhost:3000","http://localhost:5173","http://localhost:8080", "https://beautiflow.co.kr"));
                    configuration.setAllowedMethods(Collections.singletonList("*"));
                    configuration.setAllowCredentials(true);
                    configuration.setAllowedHeaders(Collections.singletonList("*"));
                    configuration.setMaxAge(3600L);
                    configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization"));
                    return configuration;
                }))

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
					"/connect/**",
                    "/users/signup",
                    "/users/refresh",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/health"
                ).permitAll()                        .anyRequest().authenticated())

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
