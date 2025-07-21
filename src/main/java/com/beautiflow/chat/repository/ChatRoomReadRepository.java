package com.beautiflow.chat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.beautiflow.chat.domain.ChatRoom;
import com.beautiflow.chat.domain.ChatRoomRead;
import com.beautiflow.user.domain.User;

public interface ChatRoomReadRepository extends JpaRepository<ChatRoomRead, Long> {

	Optional<ChatRoomRead> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);

	Optional<ChatRoomRead> findByChatRoomAndUser(ChatRoom chatRoom, User me);
}


