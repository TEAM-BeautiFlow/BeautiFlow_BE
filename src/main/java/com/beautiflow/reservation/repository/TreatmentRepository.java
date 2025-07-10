package com.beautiflow.reservation.repository;

import com.beautiflow.global.domain.TreatmentCategory;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.treatment.domain.Treatment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, Long> {
    List<Treatment> findByShop(Shop shop);
    List<Treatment> findByShopAndCategory(Shop shop, TreatmentCategory category);
    Optional<Treatment> findByShopAndId(Shop shop, Long id);
}