package com.beautiflow.treatment.domain;

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
@Table(name = "treatment_images")
public class TreatmentImage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "treatment_id", nullable = false)
	private Treatment treatment;

	@Column(nullable = false)
	private String imageUrl;

	@Column(nullable = false)
	private String originalFileName;

	@Column(nullable = false)
	private String storedFilePath;

	@Builder
	public TreatmentImage(String imageUrl, String originalFileName, String storedFilePath, Treatment treatment) {
		this.imageUrl = imageUrl;
		this.originalFileName = originalFileName;
		this.storedFilePath = storedFilePath;
		this.treatment = treatment;
	}
}