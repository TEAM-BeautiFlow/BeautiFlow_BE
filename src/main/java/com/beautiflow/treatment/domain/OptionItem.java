package com.beautiflow.treatment.domain;

import com.beautiflow.shop.dto.OptionItemUpdateReq;
import jakarta.persistence.Entity;
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
@Table(name = "option_items")
public class OptionItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private OptionGroup optionGroup;

	private String name;
	private Integer extraMinutes;
	private Integer extraPrice;
	private String description;

	public void updateDetails(OptionItemUpdateReq dto) {
		this.name = dto.name();
		this.extraPrice = dto.extraPrice();
		this.extraMinutes = dto.extraMinutes();
		this.description = dto.description();
	}

	public void setOptionGroup(OptionGroup optionGroup) {
		this.optionGroup = optionGroup;
	}
}
