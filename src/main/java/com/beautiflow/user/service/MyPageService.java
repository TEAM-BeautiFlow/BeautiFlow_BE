package com.beautiflow.user.service;

import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.dto.UserInfoReq;
import com.beautiflow.user.dto.UserInfoRes;
import com.beautiflow.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;

    public UserInfoRes getUserInfo(Long  userId ) {
        User user = userRepository.findById(userId).orElseThrow(()->new BeautiFlowException(
                UserErrorCode.USER_NOT_FOUND));

        return UserInfoRes.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .name(user.getName())
                .email(user.getEmail())
                .contact(user.getContact())
                .build();
    }

    @Transactional
    public UserInfoRes patchUserInfo(Long userId, UserInfoReq userInfoReq) {
        User user = userRepository.findById(userId).orElseThrow(()->new BeautiFlowException(
                UserErrorCode.USER_NOT_FOUND));


        user.patchUserInfo(userInfoReq.name(),  userInfoReq.email(), userInfoReq.contact());


        return UserInfoRes.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .name(user.getName())
                .email(user.getEmail())
                .contact(user.getContact())
                .build();
    }

}
