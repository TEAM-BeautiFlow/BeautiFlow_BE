package com.beautiflow.chat.dto.chatMessageDto;

import com.beautiflow.global.domain.SenderType;

public record ChatMessageSendReq(
	Long roomId,
	Long senderId,
	SenderType senderType,
	String content,
	String imageUrl
) {}
