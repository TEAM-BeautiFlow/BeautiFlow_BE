package com.beautiflow.shop.dto;

import com.beautiflow.global.domain.HolidayCycle;
import com.beautiflow.global.domain.WeekDay;
import com.beautiflow.shop.domain.RegularHoliday;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RegularHolidayDto {

  @NotNull
  private HolidayCycle cycle;

  @NotNull
  private List<WeekDay> daysOfWeek;

  public static RegularHolidayDto from(RegularHoliday holiday) {
    RegularHolidayDto dto = new RegularHolidayDto();
    dto.cycle = holiday.getCycle();
    dto.daysOfWeek = holiday.getDaysOfWeek();
    return dto;
  }
}
