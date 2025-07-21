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
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE404", "공지사항을 찾을 수 없습니다."),
    FORBIDDEN_NOTICE_ACCESS(HttpStatus.FORBIDDEN, "NOTICE403", "공지사항에 접근할 수 있는 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
