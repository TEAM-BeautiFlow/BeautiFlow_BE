// TimeSlotResponse.java
package com.beautiflow.reservation.dto;

import com.beautiflow.global.domain.ReservationStatus;
import java.time.LocalTime;
import java.util.List;

public record TimeSlotRes(
    Long reservationId,
    String customerName,
    ReservationStatus status,
    LocalTime startTime,
    LocalTime endTime,
    List<String> treatmentNames
) {}
