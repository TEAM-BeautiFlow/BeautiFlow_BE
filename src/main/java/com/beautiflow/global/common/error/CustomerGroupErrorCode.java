package com.beautiflow.global.common.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomerGroupErrorCode implements ErrorCode{
  CUSTOMER_GROUP_ERROR_CODE(HttpStatus.NOT_FOUND, "Group_001", "고객을 찾을 수 없습니다."),
  INVALID_CODE(HttpStatus.BAD_REQUEST, "GROUP_002", "잘못된 그룹 코드 형식입니다."),
  DUPLICATE_CODE(HttpStatus.CONFLICT, "GROUP_003", "이미 존재하는 그룹 코드입니다."),
  RESERVED_CODE(HttpStatus.BAD_REQUEST, "GROUP_004", "예약된 시스템 코드입니다."),
  DESIGNER_NOT_FOUND(HttpStatus.NOT_FOUND, "GROUP_005", "디자이너가 존재하지 않습니다.");


  private final HttpStatus httpStatus;
  private final String code;
  private final String message;


}