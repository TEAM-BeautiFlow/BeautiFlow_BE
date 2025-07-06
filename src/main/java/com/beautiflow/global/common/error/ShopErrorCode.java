package com.beautiflow.global.common.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShopErrorCode implements ErrorCode{

	SHOP_NOT_FOUND(HttpStatus.NOT_FOUND, "SHOP_001", "존재하지 않는 샵입니다."),
	DUPLICATE_BUSINESS_NUMBER(HttpStatus.BAD_REQUEST, "SHOP_002", "이미 등록된 사업자 등록번호입니다."),
	INVALID_SHOP_MEMBER(HttpStatus.FORBIDDEN, "SHOP_003", "해당 샵에 대한 권한이 없습니다."),
	SHOP_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "SHOP_004", "샵 멤버 정보를 찾을 수 없습니다."),
	NOT_SHOP_OWNER(HttpStatus.FORBIDDEN, "SHOP_005", "사장님만 수행할 수 있는 작업입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
