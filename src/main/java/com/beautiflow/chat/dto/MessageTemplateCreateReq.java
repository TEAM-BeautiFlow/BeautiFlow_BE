package com.beautiflow.chat.dto;

import com.beautiflow.chat.domain.SendTiming;
import com.beautiflow.global.domain.TargetGroup;

public record MessageTemplateCreateReq(
	String name,
	SendTiming sendTiming,
	int daysOffset,
	String content,
	TargetGroup targetGroup,
	boolean isActive
) {}