package com.beautiflow.global.common.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode{

  RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION400", "예약을 찾을 수 없습니다."),
  RESERVATION_TIME_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION401", "지정된 날짜의 예약을 찾을 수 없습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;


}
