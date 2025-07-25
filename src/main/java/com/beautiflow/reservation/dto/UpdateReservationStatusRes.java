package com.beautiflow.reservation.dto;

import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.reservation.domain.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateReservationStatusRes(
    Long reservationId,
    Long designerId,
    String customerName,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    ReservationStatus status
) {
  public static UpdateReservationStatusRes from(Reservation reservation) {
    return new UpdateReservationStatusRes(
        reservation.getId(),
        reservation.getDesigner().getId(),
        reservation.getCustomer().getName(),
        reservation.getReservationDate(),
        reservation.getStartTime(),
        reservation.getEndTime(),
        reservation.getStatus()
    );
  }
}
