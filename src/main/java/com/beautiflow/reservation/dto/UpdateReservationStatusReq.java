package com.beautiflow.reservation.dto;

import com.beautiflow.global.domain.ReservationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateReservationStatusReq(
    @NotNull(message = "예약 상태는 필수입니다.")
    ReservationStatus status
) {}