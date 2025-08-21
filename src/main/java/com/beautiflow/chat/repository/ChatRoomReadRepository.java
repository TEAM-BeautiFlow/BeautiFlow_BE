package com.beautiflow.chat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.beautiflow.chat.domain.ChatRoom;
import com.beautiflow.chat.domain.ChatRoomRead;
import com.beautiflow.user.domain.User;

public interface ChatRoomReadRepository extends JpaRepository<ChatRoomRead, Long> {
	Optional<ChatRoomRead> findByChatRoomAndUser(ChatRoom chatRoom, User me);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("delete from ChatRoomRead r where r.chatRoom.id = :roomId")
	void deleteByChatRoomId(@Param("roomId") Long roomId);
}
