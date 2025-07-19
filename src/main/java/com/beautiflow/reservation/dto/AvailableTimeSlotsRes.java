package com.beautiflow.reservation.dto;

import java.util.Map;

public record AvailableTimeSlotsRes(Map<String, Boolean> timeSlots) {}