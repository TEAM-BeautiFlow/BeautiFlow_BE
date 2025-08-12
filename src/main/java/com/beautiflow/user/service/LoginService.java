package com.beautiflow.user.service;

import com.beautiflow.global.common.error.CommonErrorCode;
import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.util.JWTUtil;
import com.beautiflow.global.common.util.RedisTokenUtil;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.dto.LoginReq;
import com.beautiflow.user.dto.LoginRes;
import com.beautiflow.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final RedisTokenUtil redisTokenUtil;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public LoginRes login(LoginReq loginReq) {

        if (!StringUtils.hasText(loginReq.loginKey())) {
            throw new BeautiFlowException(UserErrorCode.LOGIN_KEY_REQUIRED);
        }

        String json = redisTokenUtil.getValues(loginReq.loginKey());

        if (!StringUtils.hasText(json)) {
            throw new BeautiFlowException(UserErrorCode.LOGIN_KEY_REQUIRED);
        }

        //redisTokenUtil.deleteValues(loginReq.loginKey()); 개발환경에서 주석처리

        try {
            JsonNode node = objectMapper.readTree(json);
            String kakaoId = node.path("kakaoId").asText();
            String provider = node.path("provider").asText();
            String email = node.path("email").asText();

            boolean exists = userRepository.existsByKakaoId(kakaoId);
            System.out.println(kakaoId + ":" + provider + ":" + exists);

            if (!exists) {
                return LoginRes.builder()
                        .kakaoId(kakaoId)
                        .provider(provider)
                        .email(email)
                        .isNewUser("true")
                        .accessToken(null)
                        .refreshToken(null)
                        .build();
            } else {

                User user = userRepository.findByKakaoId(kakaoId).orElse(null);

                if (user != null) {

                    String accessToken = jwtUtil.createAccessToken(provider, kakaoId, user.getId(), email);
                    String refreshToken = jwtUtil.createRefreshToken(kakaoId, user.getId());

                    return LoginRes.builder()
                            .kakaoId(kakaoId)
                            .provider(provider)
                            .email(email)
                            .isNewUser("false")
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .build();


                } else {
                    throw new BeautiFlowException(UserErrorCode.USER_NOT_FOUND);
                }


            }
        } catch (Exception e) {
            throw new BeautiFlowException(CommonErrorCode.REDIS_CONNECTION_FAILED);
        }

    }

}
