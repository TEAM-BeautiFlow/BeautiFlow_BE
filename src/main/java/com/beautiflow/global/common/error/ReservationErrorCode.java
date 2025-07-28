package com.beautiflow.global.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReservationErrorCode implements ErrorCode{

    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RES404", "예약을 찾을 수 없습니다."),
    RESERVATION_TREATMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "RT404", "예약 시술을 찾을 수 없습니다."),
    RESERVATION_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "RO404", "예약 시술 내 옵션을 찾을 수 없습니다."),
    RESERVATION_LOCKED(HttpStatus.CONFLICT, "RES409", "해당 예약은 현재 다른 작업 중입니다. 잠시 후 다시 시도해주세요."),
    RESERVATION_CONFLICT(HttpStatus.CONFLICT, "RES410", "예약 정보에 충돌이 발생했습니다. 다시 확인해주세요."),
    RESERVATION_LOCK_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, "RES500", "락 획득 중 인터럽트 발생"),
    LOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "LOC404", "이미 해제되었거나 존재하지 않는 lock입니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}