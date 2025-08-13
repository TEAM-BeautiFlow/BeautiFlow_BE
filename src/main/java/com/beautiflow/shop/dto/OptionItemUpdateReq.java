package com.beautiflow.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record OptionItemUpdateReq(
    Long id,             //신규 추가시 null, 수정시 기존 id값

    @NotBlank(message = "옵션 아이템 이름은 필수입니다.")
    String name,

    @NotNull(message = "옵션 아이템 가격은 필수입니다.")
    @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    Integer extraPrice,
    Integer extraMinutes,
    String description
) {
}