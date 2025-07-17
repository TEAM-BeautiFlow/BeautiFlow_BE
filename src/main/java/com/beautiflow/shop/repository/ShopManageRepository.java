package com.beautiflow.shop.repository;


import com.beautiflow.shop.domain.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopManageRepository extends JpaRepository<Shop, Long> {
}