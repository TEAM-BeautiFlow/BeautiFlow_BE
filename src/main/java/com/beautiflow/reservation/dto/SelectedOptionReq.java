package com.beautiflow.reservation.dto;

import lombok.Builder;

public record SelectedOptionReq(
        Long optionGroupId,
        Long optionItemId
) {
    @Builder
    public SelectedOptionReq {}
}
