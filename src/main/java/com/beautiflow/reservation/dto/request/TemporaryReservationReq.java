package com.beautiflow.reservation.dto.request;

import java.util.List;
import lombok.Builder;

public record TemporaryReservationReq(
        Long treatmentId,
        List<SelectedOptionReq> selectedOptions
) {
    @Builder
    public TemporaryReservationReq {}
}
