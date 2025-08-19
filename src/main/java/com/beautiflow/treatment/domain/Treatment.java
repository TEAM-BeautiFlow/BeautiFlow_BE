package com.beautiflow.treatment.domain;

import com.beautiflow.treatment.dto.TreatmentUpdateReq;
import jakarta.persistence.CascadeType;
import java.util.ArrayList;
import java.util.List;

import com.beautiflow.global.domain.TreatmentCategory;
import com.beautiflow.shop.domain.Shop;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
@Table(name = "treatments")
public class Treatment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Shop shop;

	@Enumerated(EnumType.STRING)
	private TreatmentCategory category;
	private String name;
	private Integer price;
	private Integer durationMinutes;
	private String description;

	@OneToMany(mappedBy = "treatment")
	private List<TreatmentImage> images = new ArrayList<>();

	@OneToMany(mappedBy = "treatment", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OptionGroup> optionGroups = new ArrayList<>();

	public void updateTreatment(TreatmentUpdateReq requestDto) {
		if (requestDto.category() != null) this.category = requestDto.category();
		if (requestDto.name() != null) this.name = requestDto.name();
		if (requestDto.price() != null) this.price = requestDto.price();
		if (requestDto.durationMinutes() != null) this.durationMinutes = requestDto.durationMinutes();
		if (requestDto.description() != null) this.description = requestDto.description();

		if (requestDto.optionGroups() == null) {
			return;
		}

		Map<Long, OptionGroup> existingGroupsMap = this.optionGroups.stream()
				.collect(Collectors.toMap(OptionGroup::getId, Function.identity()));

		List<OptionGroup> updatedGroups = requestDto.optionGroups().stream().map(groupDto -> {
			OptionGroup optionGroup;
			if (groupDto.id() == null) {
				optionGroup = new OptionGroup(groupDto.name());
				optionGroup.setTreatment(this);
			} else {
				optionGroup = existingGroupsMap.get(groupDto.id());
				if (optionGroup == null) throw new IllegalArgumentException("존재하지 않는 옵션 그룹 ID: " + groupDto.id());
			}
			optionGroup.updateFromDto(groupDto);
			return optionGroup;
		}).toList();

		this.optionGroups.clear();
		this.optionGroups.addAll(updatedGroups);
	}
}
