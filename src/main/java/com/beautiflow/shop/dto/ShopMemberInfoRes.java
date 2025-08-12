package com.beautiflow.shop.dto;

import lombok.Builder;

@Builder
public record ShopMemberInfoRes(
        Long  shopId,
        Long userId,
        Long memberId,
        String intro,
        String imageUrl,
        String originalFileName,
        String storedFilePath


) {

}
