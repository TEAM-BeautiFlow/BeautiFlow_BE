package com.beautiflow.reservation.dto.response;


import java.util.List;
import lombok.Builder;

public record TemporaryReservationRes(
        Long id,
        String name,
        Integer durationMinutes,
        Integer price,
        List<SelectedOptionRes> selectedOptions
) {
    @Builder
    public TemporaryReservationRes {}

    public record SelectedOptionRes(
            Long optionGroupId,
            Long optionItemId
    ) {
        @Builder
        public SelectedOptionRes {}
    }
}
