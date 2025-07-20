package com.beautiflow.global.common.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TemplateErrorCode implements ErrorCode{
	TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "TEMPLATE404", "템플릿를 찾을 수 없습니다."),
	NO_TEMPLATE_PERMISSION(HttpStatus.NOT_FOUND,"TEMPLATE002", "템플릿에 대한 권한이 없습니다.");


	private final HttpStatus httpStatus;
	private final String code;
	private final String message;


}