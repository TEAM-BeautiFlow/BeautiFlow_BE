package com.beautiflow.chat.dto.chatMessageDto;

import java.util.List;

import com.beautiflow.global.domain.TargetGroup;

public record GroupMessageSendReq(
	Long shopId,
	List<String> groupCodes,
	List<Long> customerIds,
	String content,
	String imageUrl
) {}