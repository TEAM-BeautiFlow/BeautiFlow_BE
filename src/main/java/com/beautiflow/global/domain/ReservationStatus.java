package com.beautiflow.global.domain;

public enum ReservationStatus {
    TEMPORARY, // 임시 예약
    PENDING,       // 확정대기
    CONFIRMED,     // 예약 확정
    CANCELLED,     // 취소
    NO_SHOW,       // 노쇼
    COMPLETED      // 시술 완료
}
