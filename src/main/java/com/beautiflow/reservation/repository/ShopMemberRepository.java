package com.beautiflow.reservation.repository;

import com.beautiflow.global.domain.ApprovalStatus;
import com.beautiflow.global.domain.ReservationStatus;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.shop.domain.ShopMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopMemberRepository extends JpaRepository<ShopMember, Long> {
    List<ShopMember> findByShopIdAndStatus(Long shopId, ApprovalStatus status);

    Optional<ShopMember> findByShopIdAndUserIdAndStatus(Long shopId, Long userId, ApprovalStatus status);

}
