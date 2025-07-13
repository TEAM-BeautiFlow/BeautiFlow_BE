package com.beautiflow.chat.dto;

import java.time.LocalDateTime;

import com.beautiflow.chat.domain.ChatMessage;
import com.beautiflow.chat.domain.ChatRoom;
import com.beautiflow.user.domain.User;

public record ChatRoomSummaryRes(
	Long roomId,
	Long shopId,
	String shopName,
	String opponentName,
	Long opponentId,
	String lastMessageContent,
	LocalDateTime lastMessageTime
) {
	public static ChatRoomSummaryRes of(ChatRoom room, User opponent, ChatMessage lastMessage) {
		return new ChatRoomSummaryRes(
			room.getId(),
			room.getShop().getId(),
			room.getShop().getName(),
			opponent.getName(),
			opponent.getId(),
			lastMessage != null ? lastMessage.getContent() : null,
			lastMessage != null ? lastMessage.getCreatedTime() : room.getCreatedTime()
		);
	}
}