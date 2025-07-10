package com.beautiflow.reservation.dto.response;

import com.beautiflow.reservation.domain.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationDetailResponse(
    Long reservationId,
    Long designerId,
    String customerName,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    String status
) {
  public static ReservationDetailResponse from(Reservation reservation) {
    return new ReservationDetailResponse(
        reservation.getId(),
        reservation.getDesigner() != null ? reservation.getDesigner().getId() : null,
        reservation.getCustomer() != null ? reservation.getCustomer().getName() : null,
        reservation.getReservationDate(),
        reservation.getStartTime(),
        reservation.getEndTime(),
        reservation.getStatus() != null ? reservation.getStatus().name() : null
    );
  }
}
