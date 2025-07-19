package com.beautiflow.shop.repository;

import com.beautiflow.shop.domain.ShopNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopNoticeRepository extends JpaRepository<ShopNotice, Long> {
}
