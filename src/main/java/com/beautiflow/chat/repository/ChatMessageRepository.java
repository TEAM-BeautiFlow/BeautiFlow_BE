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
SELECT m FROM ChatMessage m
JOIN FETCH m.chatRoom r
JOIN FETCH r.customer
JOIN FETCH r.designer
JOIN FETCH m.sender
JOIN ChatRoomRead cr
  ON cr.chatRoom = m.chatRoom
WHERE m.createdTime <= :limitTime
  AND cr.user <> m.sender
  AND (cr.lastReadTime IS NULL OR cr.lastReadTime < m.createdTime)
  AND (cr.lastAlertSentTime IS NULL OR cr.lastAlertSentTime < :limitTime)
  AND m.createdTime = (
    SELECT MAX(m2.createdTime)
    FROM ChatMessage m2
    WHERE m2.chatRoom = m.chatRoom
      AND m2.sender <> cr.user
      AND m2.createdTime <= :limitTime
      AND (cr.lastReadTime IS NULL OR cr.lastReadTime < m2.createdTime)
  )
""")
	List<ChatMessage> findLatestUnreadMessagesForAlert(@Param("limitTime") LocalDateTime limitTime);



}