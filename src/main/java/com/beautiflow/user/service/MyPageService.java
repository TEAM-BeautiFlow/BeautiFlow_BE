package com.beautiflow.user.service;

import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.domain.GlobalRole;
import com.beautiflow.shop.domain.ShopMember;
import com.beautiflow.shop.dto.ShopMemberInfoRes;
import com.beautiflow.shop.repository.ShopMemberRepository;
import com.beautiflow.shop.repository.ShopRepository;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.dto.UserInfoReq;
import com.beautiflow.user.dto.UserInfoRes;
import com.beautiflow.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final ShopMemberRepository shopMemberRepository;

    public UserInfoRes getUserInfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BeautiFlowException(
                UserErrorCode.USER_NOT_FOUND));

        List<ShopMemberInfoRes> members = shopMemberRepository.findByUser_Id(userId).stream()
                .map(sm -> ShopMemberInfoRes.builder()
                        .shopId(sm.getShop() != null ? sm.getShop().getId() : null)
                        .userId(userId)
                        .memberId(sm.getId())
                        .intro(sm.getIntro())
                        .imageUrl(sm.getImageUrl())
                        .originalFileName(sm.getOriginalFileName())
                        .storedFilePath(sm.getStoredFilePath())
                        .build())
                .collect(Collectors.toList());

        return UserInfoRes.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .name(user.getName())
                .email(user.getEmail())
                .contact(user.getContact())
                .shopMembers(members)
                .build();
    }

    @Transactional
    public UserInfoRes patchUserInfo(Long userId, UserInfoReq userInfoReq) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BeautiFlowException(
                UserErrorCode.USER_NOT_FOUND));

        user.patchUserInfo(userInfoReq.name(), userInfoReq.email(), userInfoReq.contact());

        return UserInfoRes.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .name(user.getName())
                .email(user.getEmail())
                .contact(user.getContact())
                .build();
    }

}
