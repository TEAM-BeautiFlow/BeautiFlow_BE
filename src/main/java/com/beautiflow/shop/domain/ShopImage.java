package com.beautiflow.shop.domain;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자 보호
@Entity
@Table(name = "shop_images")
public class ShopImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String imageUrl;

  @Column(nullable = false)
  private String originalFileName;

  @Column(nullable = false)
  private String storedFilePath; // S3에서 파일을 식별하는 키

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shop_id", nullable = false)
  private Shop shop;

  @Builder
  public ShopImage(String imageUrl, String originalFileName, String storedFilePath, Shop shop) {
    this.imageUrl = imageUrl;
    this.originalFileName = originalFileName;
    this.storedFilePath = storedFilePath;
    this.shop = shop;
  }
}