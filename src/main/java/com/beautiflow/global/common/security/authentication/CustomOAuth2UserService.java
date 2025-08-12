package com.beautiflow.global.common.security.authentication;

import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.domain.GlobalRole;
import com.beautiflow.global.common.security.dto.KakaoRes;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        KakaoRes kakaoRes = KakaoRes.from(oAuth2User.getAttributes(), registrationId);
        GlobalRole globalRole = switch (kakaoRes.provider()) {
            case "kakao-customer" -> GlobalRole.CUSTOMER;
            case "kakao-staff" -> GlobalRole.STAFF;
            default -> throw new BeautiFlowException(UserErrorCode.USER_ROLE_NOT_FOUND);
        };

        //db에 user가 저장되지 않은 시점이므로 uerId가 null

        return new CustomOAuth2User(kakaoRes.provider(), kakaoRes.kakaoId(), null, globalRole);


    }

}