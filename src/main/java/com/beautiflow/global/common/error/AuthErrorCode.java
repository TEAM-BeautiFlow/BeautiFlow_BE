package com.beautiflow.global.common.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode{
	INVALID_VERIFICATION_CODE(HttpStatus.NOT_FOUND, "PHONEAUTH400", "유효하지 않은 인증번호입니다.");


	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
