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

        //이미 가입되어있는경우 (탈퇴한 유저 포함)
        if (user != null) {
            if(user.isDeleted()) {

                // 탈퇴한 경우 - deleted 필드 false로 바꾸고 이름과 전화번호 갱신
                user.reactivate(name, contact);

            } else {
                // 가입된 경우
                throw new BeautiFlowException(UserErrorCode.DUPLICATE_KAKAO_ID);
            }
        }else{
            //신규 사용자
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

        Long userId = user.getId();
        String accessToken = jwtUtil.createAccessToken(provider, kakaoId, userId);
        String refreshToken = jwtUtil.createRefreshToken(kakaoId, userId);

        return SignUpRes.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .provider(provider)
                .name(user.getName())
                .contact(user.getContact())
                .deleted(user.isDeleted())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();


    }


}
