package com.beautiflow.shop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BusinessHourUpdateReq (
    @Valid
    @Size(min = 7, max = 7, message = "일주일(7일)의 영업시간 정보가 모두 필요합니다.")
    List<BusinessHourUpdateDto> dailySchedules
) {}
