package com.beautiflow.chat.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.beautiflow.chat.domain.ChatMessage;
import com.beautiflow.chat.domain.ChatRoom;
import com.beautiflow.user.domain.User;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
	List<ChatMessage> findByChatRoomOrderByCreatedTimeAsc(ChatRoom chatRoom);

	Optional<ChatMessage> findTopByChatRoomOrderByCreatedTimeDesc(ChatRoom chatRoom);

	int countByChatRoomAndSenderNotAndCreatedTimeAfter(ChatRoom chatRoom, User sender, LocalDateTime time);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("delete from ChatMessage m where m.chatRoom.id = :roomId")
	void deleteByChatRoomId(@Param("roomId") Long roomId);
}
