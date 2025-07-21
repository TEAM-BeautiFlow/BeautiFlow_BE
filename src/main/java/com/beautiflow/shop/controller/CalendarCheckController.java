package com.beautiflow.shop.controller;

import com.beautiflow.MangedCustomer.dto.CustomerListRes;
import com.beautiflow.MangedCustomer.service.ManagedCustomerService;
import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.dto.ReservationDetailRes;
import com.beautiflow.reservation.dto.ReservationMonthRes;
import com.beautiflow.reservation.dto.TimeSlotRes;
import com.beautiflow.reservation.dto.UpdateReservationStatusReq;
import com.beautiflow.reservation.dto.UpdateReservationStatusRes;
import com.beautiflow.shop.service.CalendarCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation", description = "사장님 월별 예약 조회 API")
public class CalendarCheckController {

  private final CalendarCheckService calendarCheckService;
  private final ManagedCustomerService managedCustomerService;

  @GetMapping("/months")
  @Operation(summary = "월별 예약 유무 조회", description = "특정 월에 예약된 날짜별 예약 개수를 조회합니다.")
  public ResponseEntity<ApiResponse<List<ReservationMonthRes>>> getReservedDates(
      @AuthenticationPrincipal(expression = "userId") Long designerId,
      @RequestParam String month
  ) {
    List<ReservationMonthRes> result = calendarCheckService.getReservedDates(designerId, month);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @GetMapping("/timeslots")
  @Operation(summary = "시간대별 예약 현황 조회")
  public ResponseEntity<ApiResponse<List<TimeSlotRes>>> getReservedTimeSlots(
      @AuthenticationPrincipal(expression = "userId") Long designerId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
  ) {
    List<TimeSlotRes> result = calendarCheckService.getReservedTimeSlots(designerId, date);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @GetMapping("/{reservationId}") // 예약 상세 조회
  @Operation(summary = "예약 상세 정보 조회")
  public ResponseEntity<ApiResponse<ReservationDetailRes>> getReservationDetail(
      @PathVariable Long reservationId
  ) {
    ReservationDetailRes result = calendarCheckService.getReservationDetail(reservationId);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @PatchMapping("/{reservationId}/status")
  @Operation(summary = "예약 상태 변경 및 결과 반환")
  public ResponseEntity<ApiResponse<UpdateReservationStatusRes>> updateReservationStatus(
      @PathVariable Long reservationId,
      @RequestBody UpdateReservationStatusReq request
  ) {
    calendarCheckService.updateStatus(reservationId, request.status());

    Reservation reservation = calendarCheckService.getReservationEntity(reservationId); // 상태만 확인용
    UpdateReservationStatusRes response = UpdateReservationStatusRes.from(reservation);
    return ResponseEntity.ok(ApiResponse.success(response));
  }


  @GetMapping("/list")
  @Operation(summary = "디자이너 고객 리스트 조회")
  public ResponseEntity<ApiResponse<List<CustomerListRes>>> getCustomersByDesigner(
      @RequestParam Long designerId
  ) {
    List<CustomerListRes> customers = managedCustomerService.getCustomersByDesigner(designerId);
    return ResponseEntity.ok(ApiResponse.success(customers));
  }


}
