package com.beautiflow.chat.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.beautiflow.chat.domain.ChatMessage;
import com.beautiflow.chat.domain.ChatRoom;
import com.beautiflow.user.domain.User;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
	List<ChatMessage> findByChatRoomOrderByCreatedTimeAsc(ChatRoom chatRoom);

	Optional<ChatMessage> findTopByChatRoomOrderByCreatedTimeDesc(ChatRoom chatRoom);

	int countByChatRoomAndSenderNotAndCreatedTimeAfter(ChatRoom chatRoom, User sender, LocalDateTime time);

	@Query("""
        select m
        from ChatMessage m
        where m.chatRoom = :room
          and m.createdTime > :lastReadTime
          and m.sender <> :recipient
        order by m.createdTime asc
    """)
	Optional<ChatMessage> findOldestUnreadForRecipient(
		@Param("room") ChatRoom room,
		@Param("lastReadTime") LocalDateTime lastReadTime,
		@Param("recipient") User recipient
	);

	Optional<ChatMessage>
	findFirstByChatRoomAndCreatedTimeAfterAndSenderNotOrderByCreatedTimeAsc(
		ChatRoom room,
		LocalDateTime lastReadTime,
		User recipient
	);


}