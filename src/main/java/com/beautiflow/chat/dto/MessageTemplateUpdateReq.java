package com.beautiflow.chat.dto;

import com.beautiflow.chat.domain.SendTiming;
import com.beautiflow.chat.domain.TargetGroup;

public record MessageTemplateUpdateReq(
	String name,
	SendTiming sendTiming,
	int daysOffset,
	String content,
	TargetGroup targetGroup,
	boolean isActive
) {}
