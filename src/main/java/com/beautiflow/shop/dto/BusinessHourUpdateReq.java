package com.beautiflow.shop.dto;

import com.beautiflow.global.domain.WeekDay;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import java.util.List;

public record BusinessHourUpdateReq(
    @NotNull(message = "오픈 시간은 필수입니다.")
    LocalTime openTime, // 매장 전체 오픈 시간

    @NotNull(message = "마감 시간은 필수입니다.")
    LocalTime closeTime, // 매장 전체 마감 시간

    LocalTime breakStart, // 매장 전체 브레이크 타임 시작 (선택)

    LocalTime breakEnd, // 매장 전체 브레이크 타임 끝 (선택)

    List<WeekDay> regularClosedDays // 정기 휴무 요일 목록 (예: ["SUN", "MON"])
) {}