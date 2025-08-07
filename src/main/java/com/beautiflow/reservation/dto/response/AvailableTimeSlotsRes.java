package com.beautiflow.reservation.dto.response;

import java.util.Map;

public record AvailableTimeSlotsRes(Map<String, Boolean> timeSlots) {}