package com.beautiflow.shop.dto;

import com.beautiflow.global.domain.WeekDay;
import java.time.LocalTime;

public record BusinessHourUpdateDto(
    WeekDay dayOfWeek,
    boolean isClosed,
    LocalTime openTime,
    LocalTime closeTime,
    LocalTime breakStart,
    LocalTime breakEnd
) {}
