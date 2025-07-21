package com.beautiflow.global.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OptionErrorCode implements ErrorCode{
    OPTION_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND,"OPT_404", "옵션 항목을 찾을 수 없습니다."),
    OPTION_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND,"OPTITM_404", "옵션 그룹을 찾을 수 없습니다.");
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
