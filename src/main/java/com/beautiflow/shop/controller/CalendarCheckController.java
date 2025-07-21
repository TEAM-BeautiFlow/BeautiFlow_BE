package com.beautiflow.shop.controller;

import com.beautiflow.MangedCustomer.dto.CustomerListRes;
import com.beautiflow.MangedCustomer.service.ManagedCustomerService;
import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.security.CustomOAuth2User;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.dto.ReservationDetailRes;
import com.beautiflow.reservation.dto.ReservationListRes;
import com.beautiflow.reservation.dto.ReservationMonthRes;
import com.beautiflow.reservation.dto.UpdateReservationStatusReq;
import com.beautiflow.reservation.dto.UpdateReservationStatusRes;
import com.beautiflow.shop.service.CalendarCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class CalendarCheckController {

  private final CalendarCheckService calendarCheckService;
  private final ManagedCustomerService managedCustomerService;

  @GetMapping("/months") //월별 조회
  @Operation(summary = "월별 예약 유무 조회", description = "특정 월에 예약된 날짜별 예약 개수를 조회합니다.")
  public ResponseEntity<ApiResponse<List<ReservationMonthRes>>> getReservedDates(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @RequestParam String month
  ) {
    List<ReservationMonthRes> result = calendarCheckService.getReservedDates(customOAuth2User.getUserId(), month);
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

  @PatchMapping("/{reservationId}/status") //예약 상태 변경
  @Operation(summary = "예약 상태 변경 및 결과 반환")
  public ResponseEntity<ApiResponse<UpdateReservationStatusRes>> updateReservationStatus(
      @PathVariable Long reservationId,
      @RequestBody UpdateReservationStatusReq request
  ) {
    UpdateReservationStatusRes response = calendarCheckService.updateStatus(reservationId, request.status());
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/list") // 고객리스트 조회
  @Operation(summary = "디자이너 고객 리스트 조회")
  public ResponseEntity<ApiResponse<List<CustomerListRes>>> getCustomersByDesigner(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User
  ) {
    List<CustomerListRes> customers = managedCustomerService.getCustomersByDesigner(customOAuth2User.getUserId());
    return ResponseEntity.ok(ApiResponse.success(customers));
  }

  @GetMapping("/timeslots/paged") // 시간대별 조회 페이징
  @Operation(summary = "특정 날짜 예약 리스트 조회 (페이징)")
  public ResponseEntity<ApiResponse<Page<ReservationListRes>>> getReservationsByDate(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @PageableDefault(size = 10, sort = "startTime") Pageable pageable
  ) {
    Page<ReservationListRes> result = calendarCheckService.getReservationsByDate(customOAuth2User.getUserId(), date, pageable);
    return ResponseEntity.ok(ApiResponse.success(result));
  } //프론트에게 어떤식으로 반환값을 받고 싶은지


}
