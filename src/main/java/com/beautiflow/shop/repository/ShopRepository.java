package com.beautiflow.shop.repository;

import com.beautiflow.shop.domain.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

   boolean existsByBusinessRegistrationNumber(String buisnessRegistrationNumber);
}
