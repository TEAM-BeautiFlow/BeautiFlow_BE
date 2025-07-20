package com.beautiflow.reservation.dto.response;


import java.util.List;
import lombok.Builder;

public record TemporaryReservationRes(
        Long treatmentId,
        String name,
        Integer durationMinutes,
        Integer price,
        List<SelectedOptionRes> selectedOptions
) {
    @Builder
    public TemporaryReservationRes {}

    public record SelectedOptionRes(
            Long optionGroupId,
            Long optionItemId,
            String optionItemName,
            Integer extraMinutes,
            Integer extraPrice
    ) {
        @Builder
        public SelectedOptionRes {}
    }
}
