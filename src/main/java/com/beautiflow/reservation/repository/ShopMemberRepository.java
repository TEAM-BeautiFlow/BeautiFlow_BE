package com.beautiflow.reservation.repository;

import com.beautiflow.global.domain.ApprovalStatus;
import com.beautiflow.shop.domain.ShopMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopMemberRepository extends JpaRepository<ShopMember, Long> {
    List<ShopMember> findByShopIdAndStatus(Long shopId, ApprovalStatus status);

}
