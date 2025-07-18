package com.beautiflow.shop.dto;

import com.beautiflow.global.domain.ApprovalStatus;
import com.beautiflow.global.domain.ShopRole;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ShopRegistrationRes(
        Long id,
        String name,
        String address,
        String businessRegistrationNumber,
        ShopRegistrationRes.ShopMemberRes shopMember
) {

    @Builder
    public record ShopMemberRes(Long id,
                                Long shopId,
                                Long userId,
                                ShopRole role,
                                ApprovalStatus status,
                                LocalDateTime appliedAt,
                                LocalDateTime processedAt) {

    }


}
