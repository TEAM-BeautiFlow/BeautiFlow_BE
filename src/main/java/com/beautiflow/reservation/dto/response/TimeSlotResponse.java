package com.beautiflow.reservation.dto.response;

import java.time.LocalTime;

public record TimeSlotResponse(
    LocalTime startTime,
    LocalTime endTime
) {}
