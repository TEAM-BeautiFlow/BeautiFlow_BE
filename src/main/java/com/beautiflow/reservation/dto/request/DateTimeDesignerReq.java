package com.beautiflow.reservation.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

public record DateTimeDesignerReq(
        LocalDate date,
        LocalTime time,
        Long designerId
) {}
