package com.beautiflow.reservation.dto;

import java.time.LocalDate;
import java.util.Map;

public record AvailableDatesRes(
        Map<LocalDate, Boolean> availableDates
) {}