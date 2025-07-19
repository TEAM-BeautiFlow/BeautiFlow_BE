package com.beautiflow.reservation.dto;

import java.time.LocalDate;

public record ReservationMonthRes(
    LocalDate date,
    boolean hasReservation,
    long reservationCount
) {}
