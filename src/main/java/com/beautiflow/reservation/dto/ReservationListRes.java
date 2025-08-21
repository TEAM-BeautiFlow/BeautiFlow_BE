package com.beautiflow.reservation.dto;

import com.beautiflow.reservation.domain.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationListRes(
    Long reservationId,
    Long customerId,
    String customerName,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    String status,
    String treatmentName,
    String treatmentCategory
) {
  public static ReservationListRes from(Reservation r) {
    var treatment = (r.getReservationTreatments() == null || r.getReservationTreatments().isEmpty())
        ? null
        : r.getReservationTreatments().get(0).getTreatment();
    return new ReservationListRes(
        r.getId(),
        r.getCustomer() != null ? r.getCustomer().getId() : null,
        r.getCustomer() != null ? r.getCustomer().getName() : null,
        r.getReservationDate(), // ← 추가
        r.getStartTime(),
        r.getEndTime(),
        r.getStatus() != null ? r.getStatus().name() : null,
        treatment != null ? treatment.getName() : null,
        (treatment != null && treatment.getCategory() != null) ? treatment.getCategory().name() : null
    );
  }
}
