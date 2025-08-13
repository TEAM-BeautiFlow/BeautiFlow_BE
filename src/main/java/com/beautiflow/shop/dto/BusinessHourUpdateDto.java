package com.beautiflow.shop.dto;

import com.beautiflow.global.domain.WeekDay;
import java.time.LocalTime;

public record BusinessHourUpdateDto(
    WeekDay dayOfWeek,      // 요일 (MONDAY, TUESDAY, ...)
    boolean isClosed,       // 휴무 여부
    LocalTime openTime,     // 영업 시작 시간 (휴무일 경우 null)
    LocalTime closeTime,    // 영업 종료 시간 (휴무일 경우 null)
    LocalTime breakStart,   // 브레이크 시작 시간 (없거나 휴무일 경우 null)
    LocalTime breakEnd      // 브레이크 종료 시간 (없거나 휴무일 경우 null)
) {}
