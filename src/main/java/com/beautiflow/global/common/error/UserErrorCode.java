package com.beautiflow.global.common.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "존재하지 않는 사용자입니다."),
	DUPLICATE_KAKAO_ID(HttpStatus.BAD_REQUEST, "USER_002", "이미 가입된 카카오 계정입니다."),
	UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "USER_003", "접근 권한이 없습니다."),
	USER_ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_004", "사용자 역할 정보를 찾을 수 없습니다."),
	INVALID_USER_INPUT(HttpStatus.BAD_REQUEST, "USER_005", "잘못된 사용자 입력입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}