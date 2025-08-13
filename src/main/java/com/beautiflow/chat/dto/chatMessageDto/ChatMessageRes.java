package com.beautiflow.chat.dto.chatMessageDto;

import java.time.LocalDateTime;

import com.beautiflow.global.domain.SenderType;

public record ChatMessageRes(
	Long messageId,
	Long senderId,
	String senderName,
	SenderType senderType,
	String content,
	String imageUrl,
	LocalDateTime createdTime
) {}