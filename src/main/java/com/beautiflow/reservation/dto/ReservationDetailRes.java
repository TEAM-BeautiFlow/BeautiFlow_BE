package com.beautiflow.reservation.dto;

import com.beautiflow.global.domain.PaymentMethod;
import com.beautiflow.global.domain.PaymentStatus;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.shop.domain.Shop;
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
    int depositAmount = extractDepositAmount(reservation);
    int totalTreatmentPrice = calculateTotalTreatmentPrice(reservation);
    int totalDuration = calculateTotalDuration(reservation);
    String durationText = formatDuration(totalDuration);

    PaymentInfo paymentInfo = new PaymentInfo(
        reservation.getPaymentMethod(),
        reservation.getPaymentStatus(),
        depositAmount,
        totalTreatmentPrice - depositAmount
    );

    return new ReservationDetailRes(
        reservation.getId(),
        reservation.getDesigner() != null ? reservation.getDesigner().getId() : null,
        reservation.getCustomer() != null ? reservation.getCustomer().getName() : null,
        reservation.getReservationDate(),
        reservation.getStartTime(),
        reservation.getEndTime(),
        reservation.getStatus().name(),
        extractTreatmentNames(reservation),
        extractOptionNames(reservation),
        paymentInfo,
        extractImageUrls(reservation),
        durationText,
        reservation.getRequestNotes()
    );
  }

  private static int extractDepositAmount(Reservation reservation) {
    Shop shop = reservation.getShop();
    if (shop != null && shop.getDepositAmount() != null) {
      return shop.getDepositAmount();
    }
    return 0; // 예약금이 설정되어 있지 않으면 0 처리
  }

  private static int calculateTotalTreatmentPrice(Reservation reservation) {
    if (reservation.getReservationTreatments() == null) return 0;
    return reservation.getReservationTreatments().stream()
        .map(rt -> rt.getTreatment().getPrice() != null ? rt.getTreatment().getPrice() : 0)
        .mapToInt(Integer::intValue)
        .sum();
  }

  private static int calculateTotalDuration(Reservation reservation) {
    if (reservation.getReservationTreatments() == null) return 0;
    return reservation.getReservationTreatments().stream()
        .map(rt -> rt.getTreatment().getDurationMinutes() != null ? rt.getTreatment().getDurationMinutes() : 0)
        .mapToInt(Integer::intValue)
        .sum();
  }

  private static String formatDuration(int totalDuration) {
    if (totalDuration >= 60) {
      return (totalDuration / 60) + "시간" + (totalDuration % 60 != 0 ? " " + (totalDuration % 60) + "분" : "");
    } else {
      return totalDuration + "분";
    }
  }

  private static List<String> extractTreatmentNames(Reservation reservation) {
    if (reservation.getReservationTreatments() == null) return Collections.emptyList();
    return reservation.getReservationTreatments().stream()
        .map(rt -> rt.getTreatment().getName())
        .toList();
  }

  private static List<String> extractOptionNames(Reservation reservation) {
    if (reservation.getReservationOptions() == null) return Collections.emptyList();
    return reservation.getReservationOptions().stream()
        .map(ro -> ro.getOptionItem().getName())
        .toList();
  }

  private static List<String> extractImageUrls(Reservation reservation) {
    if (reservation.getReservationTreatments() == null) return Collections.emptyList();
    return reservation.getReservationTreatments().stream()
        .flatMap(rt -> rt.getTreatment().getImages().stream())
        .map(TreatmentImage::getImageUrl)
        .toList();
  }

  public record PaymentInfo(
      PaymentMethod method,
      PaymentStatus status,
      int depositAmount,
      int shopPayAmount
  ) {}
}
