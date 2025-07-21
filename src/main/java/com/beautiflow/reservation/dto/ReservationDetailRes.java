package com.beautiflow.reservation.dto;

import com.beautiflow.global.domain.PaymentMethod;
import com.beautiflow.global.domain.PaymentStatus;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.treatment.domain.TreatmentImage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record ReservationDetailRes(
    Long reservationId,
    Long designerId,
    String customerName,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    String status,
    List<String> treatmentNames,
    ReservationDetailRes.PaymentInfo paymentInfo, // 내부 record는 이렇게 명시적으로
    List<String> imageUrls
) {
  public static ReservationDetailRes from(Reservation reservation) {
    return new ReservationDetailRes(
        reservation.getId(),
        reservation.getDesigner() != null ? reservation.getDesigner().getId() : null,
        reservation.getCustomer() != null ? reservation.getCustomer().getName() : null,
        reservation.getReservationDate(),
        reservation.getStartTime(),
        reservation.getEndTime(),
        reservation.getStatus().name(),

        reservation.getReservationTreatments() != null
            ? reservation.getReservationTreatments().stream()
            .map(rt -> rt.getTreatment().getName())
            .collect(Collectors.toList())
            : Collections.emptyList(),

        new ReservationDetailRes.PaymentInfo( // 여기도 명확하게 지정
            reservation.getPaymentMethod(),
            reservation.getPaymentStatus()
        ),

        reservation.getReservationTreatments() != null
            ? reservation.getReservationTreatments().stream()
            .flatMap(rt -> rt.getTreatment().getImages().stream())
            .map(TreatmentImage::getImageUrl)
            .collect(Collectors.toList())
            : Collections.emptyList()
    );
  }

  public record PaymentInfo(
      PaymentMethod method,
      PaymentStatus status
  ) {}
}
