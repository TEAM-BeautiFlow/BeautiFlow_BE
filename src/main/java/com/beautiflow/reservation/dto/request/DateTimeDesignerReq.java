package com.beautiflow.reservation.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;

@Builder
public record DateTimeDesignerReq(
        LocalDate date,
        LocalTime time,
        Long designerId
) {}
