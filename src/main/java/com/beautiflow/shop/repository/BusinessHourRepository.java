package com.beautiflow.shop.repository;

import com.beautiflow.global.domain.WeekDay;
import com.beautiflow.shop.domain.BusinessHour;
import com.beautiflow.shop.domain.Shop;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessHourRepository extends JpaRepository<BusinessHour, Long> {
    Optional<BusinessHour> findByShopAndDayOfWeek(Shop shop, WeekDay weekDay);
}
