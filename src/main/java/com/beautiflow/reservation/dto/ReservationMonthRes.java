package com.beautiflow.reservation.dto;

import java.time.LocalDate;

public record ReservationMonthRes(
    long pending,    // 확정대기(PENDING)
    long completed,  // 당일완료(COMPLETED)
    long cancelled   // 당일취소(CANCELLED)
) {}
