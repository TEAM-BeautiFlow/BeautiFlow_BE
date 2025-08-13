package com.beautiflow.treatment.domain;

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
}
