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
import java.util.Objects;

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

    // 음수 방지: 총금액 < 예약금이면 0원 처리
    int shopPayAmount = Math.max(0, totalTreatmentPrice - depositAmount);

    PaymentInfo paymentInfo = new PaymentInfo(
        reservation.getPaymentMethod(),      // null 가능 → 그대로 노출
        reservation.getPaymentStatus(),      // null 가능 → 그대로 노출
        depositAmount,
        shopPayAmount
    );

    return new ReservationDetailRes(
        reservation.getId(),
        reservation.getDesigner() != null ? reservation.getDesigner().getId() : null,
        reservation.getCustomer() != null ? reservation.getCustomer().getName() : null,
        reservation.getReservationDate(),
        reservation.getStartTime(),
        reservation.getEndTime(),
        reservation.getStatus() != null ? reservation.getStatus().name() : null,
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
    Integer deposit = (shop != null) ? shop.getDepositAmount() : null;
    return deposit != null ? deposit : 0;  // 미설정 시 0 처리
  }

  private static int calculateTotalTreatmentPrice(Reservation reservation) {
    if (reservation.getReservationTreatments() == null) return 0;
    return reservation.getReservationTreatments().stream()
        .map(rt -> rt.getTreatment() != null ? rt.getTreatment().getPrice() : null)
        .filter(Objects::nonNull)
        .mapToInt(Integer::intValue)
        .sum();
  }

  private static int calculateTotalDuration(Reservation reservation) {
    if (reservation.getReservationTreatments() == null) return 0;
    return reservation.getReservationTreatments().stream()
        .map(rt -> rt.getTreatment() != null ? rt.getTreatment().getDurationMinutes() : null)
        .filter(Objects::nonNull)
        .mapToInt(Integer::intValue)
        .sum();
  }

  private static String formatDuration(int totalDuration) {
    if (totalDuration <= 0) return "0분";
    return totalDuration >= 60
        ? (totalDuration / 60) + "시간" + (totalDuration % 60 != 0 ? " " + (totalDuration % 60) + "분" : "")
        : totalDuration + "분";
  }

  private static List<String> extractTreatmentNames(Reservation reservation) {
    if (reservation.getReservationTreatments() == null) return Collections.emptyList();
    return reservation.getReservationTreatments().stream()
        .map(rt -> rt.getTreatment() != null ? rt.getTreatment().getName() : null)
        .filter(Objects::nonNull)
        .toList();
  }

  private static List<String> extractOptionNames(Reservation reservation) {
    if (reservation.getReservationOptions() == null) return Collections.emptyList();
    return reservation.getReservationOptions().stream()
        .map(ro -> ro.getOptionItem() != null ? ro.getOptionItem().getName() : null)
        .filter(Objects::nonNull)
        .toList();
  }

  private static List<String> extractImageUrls(Reservation reservation) {
    if (reservation.getReservationTreatments() == null) return Collections.emptyList();
    return reservation.getReservationTreatments().stream()
        .map(rt -> rt.getTreatment() != null ? rt.getTreatment().getImages() : null)
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .map(TreatmentImage::getImageUrl)
        .filter(Objects::nonNull)
        .toList();
  }

  public record PaymentInfo(
      PaymentMethod method,   // null 허용
      PaymentStatus status,   // null 허용
      int depositAmount,      // Shop.depositAmount
      int shopPayAmount       // 총 시술가 - 예약금 (최소 0)
  ) {}
}
