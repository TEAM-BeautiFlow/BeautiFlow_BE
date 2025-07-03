package com.beautiflow.shop.domain;

import java.util.ArrayList;
import java.util.List;

import com.beautiflow.treatment.domain.Treatment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "shops")
public class Shop {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private String contact;
	private String link;
	private String accountInfo;
	private String location;
	private String introText;
	private String mainImageUrl;

	@Column(unique = true)
	private String businessRegistrationNumber;

	@OneToMany(mappedBy = "shop")
	private List<ShopNotice> notices = new ArrayList<>();

	@OneToMany(mappedBy = "shop")
	private List<BusinessHour> businessHours = new ArrayList<>();

	@OneToMany(mappedBy = "shop")
	private List<Treatment> treatments = new ArrayList<>();
}