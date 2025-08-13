package com.beautiflow.reservation.dto;

import java.time.LocalDate;

public record ReservationMonthRes(
    long pending,
    long completed,
    long cancelled
) {}
