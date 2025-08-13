package com.beautiflow.shop.dto;

import com.beautiflow.global.domain.WeekDay;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import java.util.List;

public record BusinessHourUpdateReq(
    @NotNull(message = "오픈 시간은 필수입니다.")
    LocalTime openTime,

    @NotNull(message = "마감 시간은 필수입니다.")
    LocalTime closeTime,

    LocalTime breakStart,

    LocalTime breakEnd,

    List<WeekDay> regularClosedDays
) {}