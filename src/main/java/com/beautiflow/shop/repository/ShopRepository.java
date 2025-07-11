package com.beautiflow.shop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.beautiflow.shop.domain.Shop;
import com.beautiflow.user.domain.User;

public interface ShopRepository extends JpaRepository<Shop, Long> {

	Optional<Shop> findById(Long Id);

}
