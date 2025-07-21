package com.beautiflow.reservation.controller;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.security.CustomOAuth2User;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.dto.response.AvailableDatesRes;
import com.beautiflow.reservation.dto.response.AvailableDesignerRes;
import com.beautiflow.reservation.dto.response.AvailableTimeSlotsRes;
import com.beautiflow.reservation.dto.request.TemporaryReservationReq;
import com.beautiflow.reservation.dto.request.UpdateReservationDateTimeDesignerReq;
import com.beautiflow.reservation.service.ReservationService;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reservation", description = "고객_매장/예약")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/reservations")
@AllArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UserRepository userRepository;

    @Operation(summary = "시술 + 옵션 임시 저장", description = "예약 진행 중 선택한 시술과 옵션을 임시 저장")
    @PostMapping("/temp-save")
    public ResponseEntity<?> tempSaveReservation(
            @RequestBody TemporaryReservationReq request,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User
    ) {
        Long userId = customOAuth2User.getUserId();
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        Reservation reservation = reservationService.tempSaveReservation(customer, request);

        return ResponseEntity.ok("시술과 옵션 임시 저장에 성공했습니다 예약 ID: " + reservation.getId());
    }

    @PatchMapping("/shops/{shopId}/reservations/temp-save")
    public ResponseEntity<?> updateTempReservationTimeDesigner(
            @PathVariable Long shopId,
            @RequestBody @Valid UpdateReservationDateTimeDesignerReq request,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        Long userId = principal.getUserId();
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        reservationService.updateReservationDateTimeAndDesigner(shopId, customer, request.date(), request.time(), request.designerId());

        return ResponseEntity.ok("임시 예약의 날짜, 시간, 디자이너 정보가 성공적으로 업데이트 되었습니다.");
    }


    @Operation(summary = "30일 이내 예약 가능 날짜 조회", description = "오늘부터 30일 이내 날짜 중, 휴무일/예약 마감 제외하고 예약 가능한 날짜를 반환합니다.")
    @GetMapping("/shops/{shopId}/available-dates")
    public ResponseEntity<ApiResponse<AvailableDatesRes>> getAvailableDates(
            @Parameter(description = "조회할 shop ID", example = "1")
            @PathVariable Long shopId
    ) {
        Map<LocalDate, Boolean> availableDates = reservationService.getAvailableDates(shopId);
        AvailableDatesRes res = new AvailableDatesRes(availableDates);

        return ResponseEntity.ok(ApiResponse.success(res));
    }


    @GetMapping("/shops/{shopId}/available-times")
    public ResponseEntity<ApiResponse<AvailableTimeSlotsRes>> getAvailableTimeSlots(
            @PathVariable Long shopId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long treatmentId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User
    ) {
        Long userId = customOAuth2User.getUserId();
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));
        Map<String, Boolean> timeSlots = reservationService.getAvailableTimeSlots(shopId, date, treatmentId, customer);
        return ResponseEntity.ok(ApiResponse.success(new AvailableTimeSlotsRes(timeSlots)));
    }

    @GetMapping("/reservations/shops/{shopId}/available-designers")
    public ResponseEntity<List<AvailableDesignerRes>> getAvailableDesigners(
            @PathVariable Long shopId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {

        List<AvailableDesignerRes> available = reservationService.getAvailableDesigners(shopId, date, time);
        return ResponseEntity.ok(available);
    }



}
