package com.beautiflow.reservation.dto;

import java.util.List;
import lombok.Builder;

public record TemporaryReservationReq(
        Long shopId,
        Long treatmentId,
        List<SelectedOptionReq> selectedOptions
) {
    @Builder
    public TemporaryReservationReq {}
}
