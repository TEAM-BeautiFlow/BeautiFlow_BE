package com.beautiflow.global.common.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ManagedCustomerErrorCode implements ErrorCode{
  MANAGED_CUSTOMER_ERROR_CODE(HttpStatus.NOT_FOUND, "MATCH404", "고객을 찾을 수 없습니다.");


  private final HttpStatus httpStatus;
  private final String code;
  private final String message;


}
