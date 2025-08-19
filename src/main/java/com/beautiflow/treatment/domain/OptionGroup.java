package com.beautiflow.treatment.domain;

import com.beautiflow.shop.dto.OptionGroupUpdateReq;
import jakarta.persistence.CascadeType;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
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
@Table(name = "option_groups")
public class OptionGroup {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Treatment treatment;

	private String name;
	private boolean enabled;

	@OneToMany(mappedBy = "optionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OptionItem> items = new ArrayList<>();

	public void setName(String name) {
		this.name = name;
	}

	public void setTreatment(Treatment treatment) {
		this.treatment = treatment;
	}

	public OptionGroup(String name) {
		this.name = name;
		this.enabled = true; // 기본값으로 활성화
	}

	public void updateFromDto(OptionGroupUpdateReq dto) {
		this.name = dto.name();

		Map<Long, OptionItem> existingItemsMap = this.items.stream()
				.collect(Collectors.toMap(OptionItem::getId, Function.identity()));

		List<OptionItem> updatedItems = dto.items().stream().map(itemDto -> {
			OptionItem item;
			if (itemDto.id() == null) {
				item = OptionItem.builder()
						.name(itemDto.name())
						.extraPrice(itemDto.extraPrice())
						.extraMinutes(itemDto.extraMinutes())
						.description(itemDto.description())
						.build();
				item.setOptionGroup(this);
			} else {
				item = existingItemsMap.get(itemDto.id());
				if (item == null) throw new IllegalArgumentException("존재하지 않는 옵션 아이템 ID: " + itemDto.id());
				item.updateDetails(itemDto);
			}
			return item;
		}).toList();

		this.items.clear();
		this.items.addAll(updatedItems);
	}
}
