package com.beautiflow.shop.domain;

import java.time.LocalTime;

import com.beautiflow.global.domain.WeekDay;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "business_hours")
public class BusinessHour {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Shop shop;

	@Enumerated(EnumType.STRING)
	private WeekDay dayOfWeek;

	private LocalTime openTime;
	private LocalTime closeTime;
	private LocalTime breakStart;
	private LocalTime breakEnd;
	private boolean isClosed;
}