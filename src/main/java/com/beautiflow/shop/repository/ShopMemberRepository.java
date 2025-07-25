package com.beautiflow.shop.repository;

import com.beautiflow.shop.domain.Shop;
import com.beautiflow.shop.domain.ShopMember;
import com.beautiflow.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopMemberRepository extends JpaRepository<ShopMember,Long> {

    boolean existsByUserAndShop(User user, Shop shop);
    Optional<ShopMember> findByUserIdAndShopId(Long userId, Long shopId);
}
