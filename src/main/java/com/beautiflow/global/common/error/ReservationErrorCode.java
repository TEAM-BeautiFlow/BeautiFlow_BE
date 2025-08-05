package com.beautiflow.global.common.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode{

    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION400", "예약을 찾을 수 없습니다."),
    RESERVATION_TIME_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION401", "지정된 날짜의 예약을 찾을 수 없습니다."),
    RESERVATION_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION402", "세부내역을 찾을 수 없습니다."),
    RESERVATION_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION403", "예약상태를 확인할 수 없습니다."),
    RESERVATION_TREATMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "RT404", "예약 시술을 찾을 수 없습니다."),
    RESERVATION_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "RO404", "예약 시술 내 옵션을 찾을 수 없습니다."),
    RESERVATION_LOCKED(HttpStatus.CONFLICT, "RES409", "현재 같은 시간에 다른 예약 요청이 진행 중이어서 락을 획득할 수 없습니다."),
    RESERVATION_CONFLICT(HttpStatus.CONFLICT, "RES410", "예약 정보에 충돌이 발생했습니다. 다시 확인해주세요."),
    RESERVATION_LOCK_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, "RES500", "락 획득 중 인터럽트가 발생했습니다."),
    LOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "LOC404", "이미 해제되었거나 존재하지 않는 lock입니다."),
    TEMP_RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "TMPRES404", "임시 예약을 찾을 수 없습니다."),
    TEMP_RES_TRT_NOT_FOUND(HttpStatus.NOT_FOUND, "TMPRT404", "임시 예약 내 시술을 찾을 수 없습니다."),
    UNLOCK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "UNLOCK500", "UNLOCK에 실패했습니다."),
    RESERVATION_MISSING_DATE_TIME_DESIGNER(HttpStatus.BAD_REQUEST,"DTD400" , "예약 날짜, 시간, 디자이너 정보가 모두 필요합니다."),
    INVALID_CANCEL_STATUS(HttpStatus.BAD_REQUEST,"CANCL400" , "예약 상태가 취소할 수 있는 상태가 아닙니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;


}