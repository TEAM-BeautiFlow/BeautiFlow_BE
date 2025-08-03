package com.beautiflow.shop.domain;

import java.time.LocalDateTime;

import com.beautiflow.global.domain.ApprovalStatus;
import com.beautiflow.global.domain.ShopRole;
import com.beautiflow.user.domain.User;

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
@Table(name = "shop_members")
public class ShopMember {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String intro;

	@ManyToOne(fetch = FetchType.LAZY)
	private Shop shop;

	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@Enumerated(EnumType.STRING)
	private ShopRole role;

	@Enumerated(EnumType.STRING)
	private ApprovalStatus status;

	private String imageUrl;

	private String originalFileName;

	private String storedFilePath;

	private LocalDateTime appliedAt;

	private LocalDateTime processedAt;


	public void updateIntro(String intro) {
		this.intro = intro;
	}

	public void updateImageInfo(String imageUrl, String originalFileName, String storedFilePath) {
		this.imageUrl = imageUrl;
		this.originalFileName = originalFileName;
		this.storedFilePath = storedFilePath;
	}

	public void clearImageInfo() {
		this.imageUrl = null;
		this.originalFileName = null;
		this.storedFilePath = null;
	}


}