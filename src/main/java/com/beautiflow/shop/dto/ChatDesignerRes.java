package com.beautiflow.shop.dto;

import com.beautiflow.global.domain.ShopRole;
import com.beautiflow.shop.domain.ShopMember;

public record ChatDesignerRes(
    Long id,
    String name,
    boolean isOwner
) {
    public static ChatDesignerRes from(ShopMember member) {
        return new ChatDesignerRes(
                member.getUser().getId(),
                member.getUser().getName(),
                member.getRole() == ShopRole.OWNER
        );
    }
}