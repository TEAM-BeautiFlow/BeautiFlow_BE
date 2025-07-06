package com.beautiflow.chat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.beautiflow.chat.domain.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>{
	Optional<ChatRoom> findByShopIdAndCustomerIdAndDesignerId(Long shopId, Long customerId, Long designerId);
}
