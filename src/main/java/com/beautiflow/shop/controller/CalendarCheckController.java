package com.beautiflow.shop.controller;

import com.beautiflow.ManagedCustomer.dto.CustomerListRes;
import com.beautiflow.ManagedCustomer.service.ManagedCustomerService;
import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.CommonPageResponse;
import com.beautiflow.global.common.security.authentication.CustomOAuth2User;
import com.beautiflow.global.domain.TargetGroup;
import com.beautiflow.reservation.dto.ReservationDetailRes;
import com.beautiflow.reservation.dto.ReservationListRes;
import com.beautiflow.reservation.dto.ReservationMonthRes;
import com.beautiflow.reservation.dto.UpdateReservationStatusReq;
import com.beautiflow.reservation.dto.UpdateReservationStatusRes;
import com.beautiflow.shop.service.CalendarCheckService;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

  @GetMapping("/list")
  @Operation(summary = "디자이너 고객 리스트 조회")
  public ResponseEntity<ApiResponse<CommonPageResponse<CustomerListRes>>> getCustomersByDesigner(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) List<TargetGroup> groups,
      @ParameterObject Pageable pageable
  ) {
    Page<CustomerListRes> page = managedCustomerService.getCustomersByDesigner(
        customOAuth2User.getUserId(), keyword, groups, pageable
    );
    return ResponseEntity.ok(ApiResponse.success(CommonPageResponse.of(page)));
  }


  @GetMapping("/timeslots/paged")
  @Operation(summary = "특정 날짜 예약 리스트 조회 (페이징)")
  public ResponseEntity<ApiResponse<CommonPageResponse<ReservationListRes>>> getReservationsByDate(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @ParameterObject Pageable pageable
  ) {
    Page<ReservationListRes> page = calendarCheckService.getReservationsByDate(customOAuth2User.getUserId(), date, pageable);
    CommonPageResponse<ReservationListRes> response = CommonPageResponse.of(page);
    return ResponseEntity.ok(ApiResponse.success(response));
  }//프론트에게 어떤식으로 반환값을 받고 싶은지


}
