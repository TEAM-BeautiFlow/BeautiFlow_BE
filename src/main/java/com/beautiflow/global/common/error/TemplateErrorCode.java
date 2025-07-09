package com.beautiflow.global.common.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TemplateErrorCode implements ErrorCode{


	Template_NOT_FOUND(HttpStatus.NOT_FOUND, "TEMPLATE404", "템플릿를 찾을 수 없습니다."),
	Template_ALREADY_EXISTS(HttpStatus.FORBIDDEN, "TEMPLATE403", "이미 존재하는 템플릿입니다"),
	INVALID_Template_PARAMETER(HttpStatus.BAD_REQUEST, "TEMPLATE400", "템플릿 요청 데이터가 올바르지 않습니다.");


	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
