package com.beautiflow.shop.domain;

import com.beautiflow.shop.dto.ShopUpdateReq;
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

	private String shopName;
	private String contact;
	private String link;
	private String accountInfo;

	private String bankName;
	private String accountNumber;
	private String accountHolder;

	private String address;
	private String introduction;
	private String licenseImageUrl;

	private Integer deposit;

	@Column(unique = true)
	private String businessRegistrationNumber;

	@Column(nullable = true) // 예약금 때문에 필드 추가하였음
	private Integer depositAmount;

	@OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<ShopImage> shopImages = new ArrayList<>();

	@OneToMany(mappedBy = "shop")
	private final List<ShopNotice> notices = new ArrayList<>();

	@OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<BusinessHour> businessHours = new ArrayList<>();

	@OneToMany(mappedBy = "shop")
	private final List<Treatment> treatments = new ArrayList<>();

	@OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<RegularHoliday> regularHolidays = new ArrayList<>();


	public void updateDetails(ShopUpdateReq requestDto) {
		if (requestDto.shopName() != null) {
			this.shopName = requestDto.shopName();
		}
		if (requestDto.contact() != null) {
			this.contact = requestDto.contact();
		}
		if (requestDto.link() != null) {
			this.link = requestDto.link();
		}
		if (requestDto.accountInfo() != null) {
			this.accountInfo = requestDto.accountInfo();
		}
		if (requestDto.address() != null) {
			this.address = requestDto.address();
		}
		if (requestDto.introduction() != null) {
			this.introduction = requestDto.introduction();
		}
	}

	public void setLicenseImageUrl(String licenseImageUrl) {
		this.licenseImageUrl = licenseImageUrl;
	}
}