package com.beautiflow.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

public record UpdateReservationDateTimeDesignerReq(
        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,

        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        LocalTime time,

        @NotNull
        Long designerId
) {
    @Builder
    public UpdateReservationDateTimeDesignerReq {
    }
}
