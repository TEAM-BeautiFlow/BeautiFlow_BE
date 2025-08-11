package com.beautiflow.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.beautiflow.chat.domain.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
	Optional<ChatRoom> findByShopIdAndCustomerIdAndDesignerId(Long shopId, Long customerId, Long designerId);

	@Query("""
	SELECT r FROM ChatRoom r
	WHERE 
		(r.customer.id = :userId AND r.customerExited = false)
		OR 
		(r.designer.id = :userId AND r.designerExited = false)
	ORDER BY r.updatedTime DESC
""")
	List<ChatRoom> findMyActiveChatRooms(@Param("userId") Long userId);


	@Query("SELECT r FROM ChatRoom r " +
		"WHERE r.shop.id = :shopId AND r.customer.id = :customerId AND r.designer.id = :designerId")
	Optional<ChatRoom> findByShopAndCustomerAndDesigner(
		@Param("shopId") Long shopId,
		@Param("customerId") Long customerId,
		@Param("designerId") Long designerId);
}
