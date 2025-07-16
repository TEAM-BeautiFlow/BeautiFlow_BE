package com.beautiflow.chat.dto;

import java.util.List;

import com.beautiflow.global.domain.TargetGroup;

public record GroupMessageSendReq(
	Long shopId,
	TargetGroup targetGroup,
	List<Long> customerIds,
	String content,
	String imageUrl
) {}