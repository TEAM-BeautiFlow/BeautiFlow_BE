package com.beautiflow.user.service;

import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.util.JWTUtil;
import com.beautiflow.global.domain.GlobalRole;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.domain.UserRole;
import com.beautiflow.user.domain.UserRoleId;
import com.beautiflow.user.dto.SignUpReq;
import com.beautiflow.user.dto.SignUpRes;
import com.beautiflow.user.repository.UserRepository;
import com.beautiflow.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SignUpService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final JWTUtil jwtUtil;

    public SignUpRes signUp(SignUpReq signUpReq) {

        String kakaoId = signUpReq.kakaoId();
        String provider = signUpReq.provider();
        String name = signUpReq.name();
        String contact = signUpReq.contact();

        User user = userRepository.findByKakaoId(kakaoId).orElse(null);

        if (user == null) {
            user = User.builder()
                    .kakaoId(kakaoId)
                    .name(name)
                    .contact(contact)
                    .build();
            try {
                user = userRepository.save(user);
            } catch (Exception e) {
                throw new BeautiFlowException(UserErrorCode.USER_SAVE_FAILED);
            }
        }

        GlobalRole globalRole = switch (provider) {
            case "kakao-customer" -> GlobalRole.CUSTOMER;
            case "kakao-staff" -> GlobalRole.STAFF;
            default -> throw new BeautiFlowException(UserErrorCode.USER_ROLE_NOT_FOUND);
        };

        //기존 Role에 새로운 Role을 추가할 수 있게 함
        boolean hasSameRole = userRoleRepository.existsByUserAndRole(user, globalRole);
        if (hasSameRole) {
            throw new BeautiFlowException(UserErrorCode.DUPLICATE_KAKAO_ID);
        }

        UserRole userRole = UserRole.builder()
                .id(new UserRoleId(user.getId(), globalRole))
                .user(user)
                .role(globalRole)
                .build();
        try {
            userRoleRepository.save(userRole);
        } catch (Exception e) {
            throw new BeautiFlowException(UserErrorCode.USER_ROLE_SAVE_FAILED);
        }

        String accessToken = jwtUtil.createAccessToken(provider, kakaoId);
        String refreshToken = jwtUtil.createRefreshToken(kakaoId);

        return SignUpRes.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .provider(provider)
                .name(user.getName())
                .contact(user.getContact())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();


    }


}
