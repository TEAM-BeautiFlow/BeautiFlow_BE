package com.beautiflow.reservation.dto;

import java.time.LocalTime;

public record TimeSlotResponse(
    Long reservationId,
    String customerName,
    String status,
    LocalTime startTime,
    LocalTime endTime
) {}