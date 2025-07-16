package com.beautiflow.reservation.controller;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.reservation.dto.ReservationMonthRes;
import com.beautiflow.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation", description = "사장님 월별 예약 조회 API")
public class ReservationController {

  private final ReservationService reservationService;

  @GetMapping("/months")
  @Operation(summary = "월별 예약 유무 조회", description = "특정 월에 예약된 날짜별 예약 개수를 조회합니다.")
  public ResponseEntity<ApiResponse<List<ReservationMonthRes>>> getReservedDates(
      @RequestParam Long designerId,
      @RequestParam String month
  ) {
    List<ReservationMonthRes> result = reservationService.getReservedDates(designerId, month);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

}
