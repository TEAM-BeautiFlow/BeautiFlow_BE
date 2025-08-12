package com.beautiflow.ManagedCustomer.dto;

import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.global.domain.ReservationStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;

public record CustomerReservationItem(
    Long reservationId,
    String imageUrl,
    String shopName,
    String designerName,
    List<String> optionNames,
    String treatmentNames, // ← 시술명 추가
    LocalDate date,
    LocalTime time,
    ReservationStatus status
) {

  public static CustomerReservationItem from(Reservation reservation) {
    return new CustomerReservationItem(
        reservation.getId(),
        extractFirstImage(reservation.getStyleImageUrls()),
        reservation.getShop().getShopName(),
        reservation.getDesigner().getName(),
        reservation.getReservationOptions().stream()
            .map(ro -> ro.getOptionItem().getName())
            .toList(),
        reservation.getReservationTreatments().stream()
            .findFirst() // 첫 번째만 꺼내기
            .map(rt -> rt.getTreatment().getName())
            .orElse(null),
        reservation.getReservationDate(),
        reservation.getStartTime(),
        reservation.getStatus()
    );
  }

  private static String extractFirstImage(String jsonString) {
    if (jsonString == null || jsonString.isBlank()) return null;

    try {
      // ["url1", "url2"] 형태라 가정
      ObjectMapper mapper = new ObjectMapper();
      List<String> urls = mapper.readValue(jsonString, new TypeReference<>() {});
      return urls.isEmpty() ? null : urls.get(0);
    } catch (Exception e) {
      return null; // 파싱 실패 시 null 반환
    }
  }
}
