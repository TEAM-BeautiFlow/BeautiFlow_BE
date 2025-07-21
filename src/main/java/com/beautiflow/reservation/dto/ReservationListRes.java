package com.beautiflow.reservation.dto;

import com.beautiflow.reservation.domain.Reservation;
import java.time.LocalTime;

public record ReservationListRes(
    Long reservationId,
    String customerName,
    LocalTime startTime,
    LocalTime endTime,
    String status,
    String treatmentName,
    String treatmentCategory
) {
  public static ReservationListRes from(Reservation r) {
    var treatment = r.getReservationTreatments().isEmpty()
        ? null
        : r.getReservationTreatments().get(0).getTreatment();
    return new ReservationListRes(
        r.getId(),
        r.getCustomer().getName(),
        r.getStartTime(),
        r.getEndTime(),
        r.getStatus().name(),
        treatment != null ? treatment.getName() : null,
        treatment != null ? treatment.getCategory().name() : null
    );
  }
}
