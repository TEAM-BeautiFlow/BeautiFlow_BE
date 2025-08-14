package com.beautiflow.shop.domain;

import com.beautiflow.global.domain.HolidayCycle;
import com.beautiflow.global.domain.WeekDay;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "regular_holidays")
public class RegularHoliday {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shop_id")
  private Shop shop;

  @Enumerated(EnumType.STRING)
  private HolidayCycle cycle;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "regular_holiday_days",
      joinColumns = @JoinColumn(name = "regular_holiday_id")
  )
  @Enumerated(EnumType.STRING)
  @Column(name = "day_of_week")
  private List<WeekDay> daysOfWeek = new ArrayList<>();

  public void setShop(Shop shop) {
    this.shop = shop;
  }
}