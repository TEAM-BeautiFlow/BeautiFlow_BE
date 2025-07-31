package com.beautiflow.treatment.domain;

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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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

	@OneToMany(mappedBy = "treatment")
	private List<OptionGroup> optionGroups = new ArrayList<>();
}
