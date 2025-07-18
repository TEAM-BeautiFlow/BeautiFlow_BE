package com.beautiflow.shop.dto;

import com.beautiflow.global.domain.ApprovalStatus;
import lombok.Builder;

@Builder
public record ShopApplyRes(
        ApprovalStatus status,
        Long shopMemberId,
        Long userId,
        Long shopId

        ) {

}
