package com.beautiflow.global.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ManagedCustomerErrorCode implements ErrorCode{
  LIST_NOT_FOUND(HttpStatus.NOT_FOUND, "MANAGED400", "리스트를 찾을 수 없습니다.");


  private final HttpStatus httpStatus;
  private final String code;
  private final String message;


}