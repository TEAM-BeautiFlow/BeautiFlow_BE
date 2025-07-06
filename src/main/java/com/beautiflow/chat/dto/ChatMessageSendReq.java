package com.beautiflow.chat.dto;

import com.beautiflow.global.domain.SenderType;

public record ChatMessageSendReq(
	Long senderId,
	SenderType senderType,
	String content,
	String imageUrl
) {}