package com.beautiflow.shop.converter;

import com.beautiflow.global.domain.WeekDay;
import java.time.DayOfWeek;

public class WeekDayConverter {

    public static WeekDay toWeekDay(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case SUNDAY -> WeekDay.SUN;
            case MONDAY -> WeekDay.MON;
            case TUESDAY -> WeekDay.TUE;
            case WEDNESDAY -> WeekDay.WED;
            case THURSDAY -> WeekDay.THU;
            case FRIDAY -> WeekDay.FRI;
            case SATURDAY -> WeekDay.SAT;
        };
    }
}