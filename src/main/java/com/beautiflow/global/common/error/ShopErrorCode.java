package com.beautiflow.global.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ShopErrorCode implements ErrorCode{

    SHOP_NOT_FOUND(HttpStatus.NOT_FOUND, "SHOP404", "매장을 찾을 수 없습니다."),
    INVALID_SHOP_PARAMETER(HttpStatus.BAD_REQUEST, "SHOP400", "유효하지 않은 매장 파라미터입니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "IMAGE404", "해당 이미지를 찾을 수 없습니다."),
    UNAUTHORIZED_SHOP_ACCESS(HttpStatus.FORBIDDEN, "SHOP403", "해당 매장에 접근할 수 있는 권한이 없습니다."),
    SHOP_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "SHOP405", "이미 등록된 매장입니다."),
    ALREADY_SHOP_MEMBER(HttpStatus.BAD_REQUEST,"SHOP406","이미 해당 매장의 직원으로 등록되어 있습니다."),
    ACCESS_DENIED_SHOP_ROLE(HttpStatus.BAD_REQUEST,"SHOP407", "원장만 가능한 기능입니다."),
    SHOP_MEMBER_NOT_APPROVED(HttpStatus.BAD_REQUEST,"SHOP408","아직 승인되지 않은 직원입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
