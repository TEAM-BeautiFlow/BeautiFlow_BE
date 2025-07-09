package com.beautiflow.reservation.controller;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.reservation.dto.response.ReservationDetailResponse;
import com.beautiflow.reservation.dto.response.TimeSlotResponse;
import com.beautiflow.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

  private final ReservationService reservationService;

  @GetMapping("/dates") //날짜별 예약 유무 캘린더
  @Operation(summary = "예약 유무 날짜 조회")
  public ApiResponse<List<LocalDate>> getReservedDates(
      @RequestParam Long designerId,
      @RequestParam String month
  ) {
    return ApiResponse.success(reservationService.getReservedDates(designerId, month));
  }


}
