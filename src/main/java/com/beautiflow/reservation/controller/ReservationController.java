package com.beautiflow.reservation.controller;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.reservation.dto.response.ReservationDetailResponse;
import com.beautiflow.reservation.dto.response.TimeSlotResponse;
import com.beautiflow.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

  private final ReservationService reservationService;

  @GetMapping("/dates") // 날짜별 예약 유무 캘린더
  @Operation(summary = "예약 유무 날짜 조회")
  public ResponseEntity<ApiResponse<List<LocalDate>>> getReservedDates(
      @RequestParam Long designerId,
      @RequestParam String month
  ) {
    List<LocalDate> result = reservationService.getReservedDates(designerId, month);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @GetMapping("/timeslots") // 시간대별 예약 현황 조회
  @Operation(summary = "시간대별 예약 현황 조회")
  public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> getReservedTimeSlots(
      @RequestParam Long designerId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
  ) {
    List<TimeSlotResponse> result = reservationService.getReservedTimeSlots(designerId, date);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @GetMapping("/{reservationId}") // 예약 상세 조회
  @Operation(summary = "예약 상세 정보 조회")
  public ResponseEntity<ApiResponse<ReservationDetailResponse>> getReservationDetail(
      @PathVariable Long reservationId
  ) {
    ReservationDetailResponse result = reservationService.getReservationDetail(reservationId);
    return ResponseEntity.ok(ApiResponse.success(result));
  }
}
