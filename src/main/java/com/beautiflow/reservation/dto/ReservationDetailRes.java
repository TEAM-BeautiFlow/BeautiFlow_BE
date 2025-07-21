package com.beautiflow.reservation.dto;

import com.beautiflow.global.domain.PaymentMethod;
import com.beautiflow.global.domain.PaymentStatus;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.treatment.domain.TreatmentImage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

public record ReservationDetailRes(
    Long reservationId,
    Long designerId,
    String customerName,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    String status,
    List<String> treatmentNames,
    List<String> optionNames,
    PaymentInfo paymentInfo,
    List<String> imageUrls,
    String durationText,
    String requestNotes
) {
  public static ReservationDetailRes from(Reservation reservation) {
    final int RESERVATION_DEPOSIT = 5000;

    int totalTreatmentPrice = reservation.getReservationTreatments() != null
        ? reservation.getReservationTreatments().stream()
        .map(rt -> rt.getTreatment().getPrice() != null ? rt.getTreatment().getPrice() : 0)
        .mapToInt(Integer::intValue)
        .sum()
        : 0;

    int totalDuration = reservation.getReservationTreatments() != null
        ? reservation.getReservationTreatments().stream()
        .map(rt -> rt.getTreatment().getDurationMinutes() != null ? rt.getTreatment().getDurationMinutes() : 0)
        .mapToInt(Integer::intValue)
        .sum()
        : 0;

    String durationText = totalDuration >= 60
        ? (totalDuration / 60) + "시간" + (totalDuration % 60 != 0 ? " " + (totalDuration % 60) + "분" : "")
        : totalDuration + "분";

    PaymentInfo paymentInfo = new PaymentInfo(
        reservation.getPaymentMethod(),
        reservation.getPaymentStatus(),
        RESERVATION_DEPOSIT,
        totalTreatmentPrice - RESERVATION_DEPOSIT
    );

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
            .toList()
            : Collections.emptyList(),
        reservation.getReservationOptions() != null
            ? reservation.getReservationOptions().stream()
            .map(ro -> ro.getOptionItem().getName())
            .toList()
            : Collections.emptyList(),
        paymentInfo,
        reservation.getReservationTreatments() != null
            ? reservation.getReservationTreatments().stream()
            .flatMap(rt -> rt.getTreatment().getImages().stream())
            .map(TreatmentImage::getImageUrl)
            .toList()
            : Collections.emptyList(),
        durationText,
        reservation.getRequestNotes()
    );
  }

  public record PaymentInfo(
      PaymentMethod method,
      PaymentStatus status,
      int depositAmount,
      int shopPayAmount
  ) {}
}

