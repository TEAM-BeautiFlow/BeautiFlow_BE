package com.beautiflow.shop.repository;

import com.beautiflow.shop.domain.ShopImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopImageRepository extends JpaRepository<ShopImage, Long> {
}