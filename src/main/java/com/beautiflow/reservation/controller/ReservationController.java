package com.beautiflow.reservation.controller;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.error.TreatmentErrorCode;
import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.lock.ReservationLockManager;
import com.beautiflow.global.common.security.authentication.CustomOAuth2User;
import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.reservation.domain.TempReservation;
import com.beautiflow.reservation.dto.request.DateTimeDesignerReq;
import com.beautiflow.reservation.dto.request.RequestNotesStyleReq;
import com.beautiflow.reservation.dto.request.TmpReservationReq;
import com.beautiflow.reservation.dto.request.TreatOptionReq;
import com.beautiflow.reservation.dto.response.AvailableDatesRes;
import com.beautiflow.reservation.dto.response.AvailableDesignerRes;
import com.beautiflow.reservation.dto.response.AvailableTimeSlotsRes;
import com.beautiflow.reservation.dto.response.MyReservInfoRes;
import com.beautiflow.reservation.dto.response.ReservationStatusRes;
import com.beautiflow.reservation.repository.TreatmentRepository;
import com.beautiflow.reservation.service.ReservationService;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.treatment.domain.Treatment;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.repository.UserRepository;
import com.nimbusds.oauth2.sdk.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.hibernate.sql.Update;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    private final TreatmentRepository treatmentRepository;
    private final ReservationLockManager reservationLockManager;

    @PostMapping("/{shopId}/process")
    @Operation(summary = "통합 예약 처리 API",
            description = "임시 저장, 수정, 삭제, 최종 저장, 락 해제를 한 API에서 처리")
    public ResponseEntity<?> processReservation(
            @Parameter(
                    description = "매장 ID",
                    required = true,
                    schema = @Schema(type = "integer", format = "int64", example = "1")
            )
            @PathVariable(name = "shopId") Long shopId,
            @RequestBody TmpReservationReq request,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) throws InterruptedException {
        Long userId = principal.getUserId();
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        reservationService.processReservationFlow(shopId, customer, request);

        return ResponseEntity.ok("예약 작업이 성공적으로 처리되었습니다.");
    }

    @Operation(summary = "30일 이내 예약 가능 날짜 조회", description = "오늘부터 30일 이내 날짜 중, 휴무일/예약 마감 제외하고 예약 가능한 날짜를 반환합니다.")
    @GetMapping("/shops/{shopId}/available-dates")
    public ResponseEntity<ApiResponse<AvailableDatesRes>> getAvailableDates(
            @Parameter(
                    description = "매장 ID",
                    required = true,
                    schema = @Schema(type = "integer", format = "int64", example = "1")
            )
            @PathVariable(name = "shopId") Long shopId
    ) {
        Map<LocalDate, Boolean> availableDates = reservationService.getAvailableDates(shopId);

        return ResponseEntity.ok(ApiResponse.success(new AvailableDatesRes(availableDates)));
    }



    @GetMapping("/shops/{shopId}/available-times")
    @Operation(
            summary = "예약 가능한 시간 조회",
            description = "특정 매장의 특정 날짜와 시술에 대해 예약 가능한 시간대를 조회합니다."
    )
    public ResponseEntity<ApiResponse<AvailableTimeSlotsRes>> getAvailableTimeSlots(
            @Parameter(
                    description = "매장 ID",
                    required = true,
                    schema = @Schema(type = "integer", format = "int64", example = "1")
            )
            @PathVariable Long shopId,

            @Parameter(
                    description = "예약 날짜",
                    required = true,
                    schema = @Schema(type = "string", format = "date", example = "2024-01-15")
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            @Parameter(
                    description = "시술 ID",
                    required = true,
                    schema = @Schema(type = "integer", format = "int64", example = "10")
            )
            @RequestParam(name = "treatmentId") Long treatmentId,

            @AuthenticationPrincipal CustomOAuth2User customOAuth2User
    ) {
        Long userId = customOAuth2User.getUserId();
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));
        if (treatmentId == null) {
            throw new BeautiFlowException(TreatmentErrorCode.INVALID_TREATMENT_PARAMETER);
        }
        Map<String, Boolean> timeSlots = reservationService.getAvailableTimeSlots(shopId, date, treatmentId, customer);
        return ResponseEntity.ok(ApiResponse.success(new AvailableTimeSlotsRes(timeSlots)));
    }

    @GetMapping("/shops/{shopId}/available-designers")
    @Operation(
            summary = "예약 가능한 디자이너 조회",
            description = "특정 매장의 특정 날짜와 시간에 예약 가능한 디자이너 목록을 조회합니다."
    )
    public ResponseEntity<List<AvailableDesignerRes>> getAvailableDesigners(
            @Parameter(
                    description = "매장 ID",
                    required = true,
                    schema = @Schema(type = "integer", format = "int64", example = "1")
            )
            @PathVariable Long shopId,

            @Parameter(
                    description = "예약 날짜",
                    required = true,
                    schema = @Schema(type = "string", format = "date", example = "2024-01-15")
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            @Parameter(
                    description = "예약 시간",
                    required = true,
                    schema = @Schema(type = "string", format = "time", example = "14:00")
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time
    ) {

        List<AvailableDesignerRes> available = reservationService.getAvailableDesigners(shopId, date, time);
        return ResponseEntity.ok(available);
    }

    @GetMapping("/shops/{shopId}/my-reserv-info")
    @Operation(
            summary = "내 임시 예약 정보 조회",
            description = "특정 매장에서의 내 임시 예약 정보를 조회합니다."
    )
    public ResponseEntity<ApiResponse<MyReservInfoRes>> getMyReservInfo(
            @Parameter(
                    description = "매장 ID",
                    required = true,
                    schema = @Schema(type = "integer", format = "int64", example = "1")
            )
            @PathVariable(name = "shopId") Long shopId,
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User
    ) {
        Long userId = customOAuth2User.getUserId();
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

        MyReservInfoRes res = reservationService.myReservInfo(shopId, customer);

        return ResponseEntity.ok(ApiResponse.success(res));

    }

    @GetMapping("/my-reservation")
    @Operation(
            summary = "상태별 내 예약 목록 조회",
            description = "예약 상태에 따른 내 예약 목록을 조회합니다."
    )
    public ResponseEntity<List<ReservationStatusRes>> getReservationByStatus(
            @Parameter(
                    description = "예약 상태",
                    required = true,
                    schema = @Schema(
                            type = "string",
                            allowableValues = {"PENDING", "CONFIRMED", "CANCELLED", "NO_SHOW", "COMPLETED"},
                            example = "CONFIRMED"
                    )
            )
            @RequestParam(name = "status") ReservationStatus status
    ){
        List<ReservationStatusRes> result = reservationService.getReservationsByStatus(status);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{reservationId}/cancel")
    @Operation(
            summary = "예약 취소",
            description = "예약 ID로 예약을 취소합니다."
    )
    public ResponseEntity<String> cancelReservation(
            @Parameter(
                    description = "예약 ID",
                    required = true,
                    schema = @Schema(type = "integer", format = "int64", example = "100")
            )
            @PathVariable(name = "reservationId") Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok("예약이 성공적으로 취소되었습니다: " + reservationId);
    }

}
