package com.beautiflow.global.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TreatmentErrorCode implements ErrorCode {

    TREATMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "TREATMENT404", "시술을 찾을 수 없습니다."),
    INVALID_TREATMENT_PARAMETER(HttpStatus.BAD_REQUEST, "TREATMENT400", "유효하지 않은 시술 파라미터입니다."),
    UNAUTHORIZED_TREATMENT_ACCESS(HttpStatus.FORBIDDEN, "TREATMENT403", "해당 시술에 접근할 수 있는 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}