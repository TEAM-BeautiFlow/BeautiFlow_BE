package com.beautiflow.global.common.security.authentication;

import com.beautiflow.global.common.util.JWTUtil;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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

    public CustomSuccessHandler(JWTUtil jwtUtil, UserRepository userRepository) {

        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        //OAuth2User
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String kakaoId = oAuth2User.getKakaoId();
        String provider = oAuth2User.getProvider();

        User user = userRepository.findByKakaoId(kakaoId).orElse(null);


        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");


        if (user==null) {
            response.addCookie(createCookie("isUserAlreadyExist", "false"));
            response.addCookie(createCookie("kakaoId", kakaoId));
            response.addCookie(createCookie("provider", provider));
            response.sendRedirect("http://localhost:5173");
        } else {
            Long userId = user.getId();
            response.addCookie(createCookie("isUserAlreadyExist", "true"));
            response.addCookie(createCookie("kakaoId", kakaoId));
            response.addCookie(createCookie("provider", provider));
            String accessToken = jwtUtil.createAccessToken(provider, kakaoId, userId);
            String refreshToken = jwtUtil.createRefreshToken(kakaoId, userId);
            response.addCookie(createCookie("accessToken", accessToken));
            response.addCookie(createCookie("refreshToken", refreshToken));
            response.sendRedirect("http://localhost:5173");
        }


    }


    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        return cookie;
    }
}