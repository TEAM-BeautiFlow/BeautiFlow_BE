package com.beautiflow.shop.dto;

import com.beautiflow.global.domain.WeekDay;
import com.beautiflow.shop.domain.BusinessHour;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BusinessHourRes {

  private LocalTime openTime;
  private LocalTime closeTime;
  private LocalTime breakStart;
  private LocalTime breakEnd;

  public static BusinessHourRes from(List<BusinessHour> businessHours) {
    if (businessHours == null || businessHours.isEmpty()) {
      return null;
    }

    BusinessHour representativeDay = businessHours.stream()
        .filter(bh -> !bh.isClosed() && bh.getOpenTime() != null)
        .findFirst()
        .orElse(null);

    return BusinessHourRes.builder()
        .openTime(representativeDay != null ? representativeDay.getOpenTime() : null)
        .closeTime(representativeDay != null ? representativeDay.getCloseTime() : null)
        .breakStart(representativeDay != null ? representativeDay.getBreakStart() : null)
        .breakEnd(representativeDay != null ? representativeDay.getBreakEnd() : null)
        .build();
  }
}