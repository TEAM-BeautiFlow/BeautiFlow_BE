package com.beautiflow.global.common.security.authentication;

import com.beautiflow.global.common.util.JWTUtil;
import com.beautiflow.global.common.util.RedisTokenUtil;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RedisTokenUtil redisTokenUtil;
    private final ObjectMapper om = new ObjectMapper();

    //임시로 유효기간 길게
    private static final Duration LOGIN_KEY_TTL = Duration.ofMinutes(80);


    public CustomSuccessHandler( UserRepository userRepository,
            RedisTokenUtil redisTokenUtil) {

        this.userRepository = userRepository;
        this.redisTokenUtil = redisTokenUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String kakaoId = oAuth2User.getKakaoId();
        String provider = oAuth2User.getProvider();
        String email = oAuth2User.getEmail();

        boolean exists = userRepository.findByKakaoId(kakaoId).isPresent();

        Map<String, Object> payload = new HashMap<>();
        payload.put("kakaoId", kakaoId);
        payload.put("provider", provider);
        payload.put("isUserAlreadyExist", exists);
        payload.put("email", email);

        String loginKey = "login:" + UUID.randomUUID();
        redisTokenUtil.setValues(loginKey, om.writeValueAsString(payload), LOGIN_KEY_TTL);

        response.setStatus(HttpServletResponse.SC_FOUND);
        String frontCallbackUrl = "http://localhost:5173";
        response.setHeader("Location", frontCallbackUrl + "?loginKey=" + loginKey);
    }
}