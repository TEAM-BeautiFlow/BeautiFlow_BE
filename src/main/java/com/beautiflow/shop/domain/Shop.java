package com.beautiflow.shop.domain;

import jakarta.persistence.CascadeType;
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
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shops")
public class Shop {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String shopName;
	private String contact;
	private String link;
	private String accountInfo;
	private String address;
	private String introduction;

	@Column(unique = true)
	private String businessRegistrationNumber;

	@OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<ShopImage> shopImages = new ArrayList<>();

	@OneToMany(mappedBy = "shop")
	private final List<ShopNotice> notices = new ArrayList<>();

	@OneToMany(mappedBy = "shop")
	private final List<BusinessHour> businessHours = new ArrayList<>();

	@OneToMany(mappedBy = "shop")
	private final List<Treatment> treatments = new ArrayList<>();
}