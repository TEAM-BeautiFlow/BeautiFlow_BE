package com.beautiflow.reservation.repository;

import com.beautiflow.global.domain.GlobalRole;
import com.beautiflow.user.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignerRepository extends JpaRepository<User, Long> {

    // 특정 매장(shopId)의 STAFF (디자이너) 목록 조회
    List<User> findByShopMemberships_Shop_IdAndRoles_Id_Role(Long shopId, GlobalRole role);
}