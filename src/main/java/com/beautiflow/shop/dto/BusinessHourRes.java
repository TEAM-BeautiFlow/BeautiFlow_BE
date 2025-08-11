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
  private List<WeekDay> regularClosedDays;

  // BusinessHour 엔티티 리스트를 이 DTO로 변환하는 정적 팩토리 메서드
  public static BusinessHourRes from(List<BusinessHour> businessHours) {
    if (businessHours == null || businessHours.isEmpty()) {
      return null; // 또는 기본값 반환
    }

    // 영업일 중 첫 번째 날을 기준으로 대표 시간을 찾음
    BusinessHour representativeDay = businessHours.stream()
        .filter(bh -> !bh.isClosed() && bh.getOpenTime() != null)
        .findFirst()
        .orElse(null);

    // 휴무일 목록을 추출
    List<WeekDay> closedDays = businessHours.stream()
        .filter(BusinessHour::isClosed)
        .map(BusinessHour::getDayOfWeek)
        .collect(Collectors.toList());

    return BusinessHourRes.builder()
        .openTime(representativeDay != null ? representativeDay.getOpenTime() : null)
        .closeTime(representativeDay != null ? representativeDay.getCloseTime() : null)
        .breakStart(representativeDay != null ? representativeDay.getBreakStart() : null)
        .breakEnd(representativeDay != null ? representativeDay.getBreakEnd() : null)
        .regularClosedDays(closedDays)
        .build();
  }
}