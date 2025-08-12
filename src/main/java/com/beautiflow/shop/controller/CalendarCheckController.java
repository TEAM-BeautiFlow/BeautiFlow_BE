package com.beautiflow.shop.controller;

import com.beautiflow.ManagedCustomer.service.ManagedCustomerService;
import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.CommonPageResponse;
import com.beautiflow.global.common.security.authentication.CustomOAuth2User;
import com.beautiflow.reservation.dto.ReservationDetailRes;
import com.beautiflow.reservation.dto.ReservationListRes;
import com.beautiflow.reservation.dto.ReservationMonthRes;
import com.beautiflow.reservation.dto.UpdateReservationStatusReq;
import com.beautiflow.reservation.dto.UpdateReservationStatusRes;
import com.beautiflow.shop.service.CalendarCheckService;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class CalendarCheckController {

  private final CalendarCheckService calendarCheckService;

  @GetMapping("/months") // 요청 파라미터 없이 당일 3개만 반환
  @Operation(summary = "당일 예약 현황", description = "오늘 기준 PENDING/COMPLETED/CANCELLED 건수를 반환합니다.")
  public ResponseEntity<ApiResponse<ReservationMonthRes>> getTodaySummary(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User
  ) {
    ReservationMonthRes result =
        calendarCheckService.getTodaySummary(customOAuth2User.getUserId());
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


  @GetMapping("/timeslots/paged")
  @Operation(summary = "월별 예약 리스트 조회 (페이징)", description = "요청한 월(yyyy-MM)의 예약을 날짜 포함해 페이지로 반환합니다.")
  public ResponseEntity<ApiResponse<CommonPageResponse<ReservationListRes>>> getReservationsByMonth(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
      @ParameterObject Pageable pageable
  ) {
    // 기본 정렬: 날짜 ASC → 시작시간 ASC
    Pageable sortedPageable = PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        Sort.by("reservationDate").ascending()
            .and(Sort.by("startTime").ascending())
    );

    Page<ReservationListRes> page =
        calendarCheckService.getReservationsByMonth(customOAuth2User.getUserId(), month, sortedPageable);

    return ResponseEntity.ok(ApiResponse.success(CommonPageResponse.of(page)));
  }





}
