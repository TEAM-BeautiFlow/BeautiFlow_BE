package com.beautiflow.user.dto;

import com.beautiflow.shop.dto.ShopMemberInfoRes;
import java.util.List;
import lombok.Builder;

@Builder
public record UserInfoRes(
        Long id,
        String kakaoId,
        String name,
        String contact,
        String email,
        List<Long> shopId,
        List<ShopMemberInfoRes> shopMembers
) {

}
