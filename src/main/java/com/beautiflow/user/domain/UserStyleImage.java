package com.beautiflow.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "user_style_images")
public class UserStyleImage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String imageUrl;

	@Column(nullable = false)
	private String originalFileName;

	@Column(nullable = false)
	private String storedFilePath;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_style_id", nullable = false)
	private UserStyle userStyle;

	private String extractFileName(String url) {
		return url.substring(url.lastIndexOf("/") + 1);
	}



}
