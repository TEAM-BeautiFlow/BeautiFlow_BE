package com.beautiflow.global.common.security;

import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.util.JWTUtil;
import com.beautiflow.global.domain.GlobalRole;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.dto.LoginRes;
import com.beautiflow.user.dto.UserRes;
import com.beautiflow.user.repository.UserRepository;
import com.beautiflow.user.repository.UserRoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomSuccessHandler(JWTUtil jwtUtil, UserRepository userRepository,
            UserRoleRepository userRoleRepository) {

        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        //OAuth2User
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String kakaoId = oAuth2User.getKakaoId();
        String provider = oAuth2User.getProvider();

        GlobalRole globalRole = switch (provider) {
            case "kakao-customer" -> GlobalRole.CUSTOMER;
            case "kakao-staff" -> GlobalRole.STAFF;
            default -> throw new BeautiFlowException(UserErrorCode.USER_ROLE_NOT_FOUND);
        };

        User user = userRepository.findByKakaoId(kakaoId).orElse(null);

        // 이미 같은 역할로 가입한 사용자인지 판단
        boolean hasSameRole = userRoleRepository.existsByUserAndRole(user, globalRole);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        if (!hasSameRole) {
            // 가입되지 않은 유저, json body로 응답
            var body = new LoginRes(null, null, null, false, kakaoId, provider);
            response.getWriter().write(objectMapper.writeValueAsString(body));
        } else {
            // 이미 가입된 유저 토큰 발급, json body로 응답
            String accessToken = jwtUtil.createAccessToken(provider, kakaoId);
            String refreshToken = jwtUtil.createRefreshToken(kakaoId);
            var userRes = new UserRes(user.getId(), user.getName(), user.getContact());
            var body = new LoginRes(accessToken, refreshToken, userRes, true, kakaoId, provider);
            response.getWriter().write(objectMapper.writeValueAsString(body));
        }
    }
}