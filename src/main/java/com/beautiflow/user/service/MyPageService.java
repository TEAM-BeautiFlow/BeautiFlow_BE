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
import java.util.ArrayList;
import java.util.Iterator;
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


        List<Long> shopIds = new ArrayList<>();

        for (ShopMember sm : shopMemberRepository.findByUser_Id(userId)) {
            shopIds.add(sm.getShop() != null ? sm.getShop().getId() : null);
        }

        return UserInfoRes.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .name(user.getName())
                .email(user.getEmail())
                .contact(user.getContact())
                .shopId(shopIds)
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
