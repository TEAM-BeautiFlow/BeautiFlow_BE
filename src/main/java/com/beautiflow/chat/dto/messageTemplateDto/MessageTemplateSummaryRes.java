package com.beautiflow.chat.dto.messageTemplateDto;

import com.beautiflow.chat.domain.SendTiming;
import com.beautiflow.global.domain.TargetGroup;

import lombok.Builder;

@Builder
public record MessageTemplateSummaryRes(
	Long id,
	boolean isActive,
	String name,
	SendTiming sendTiming,
	int daysOffset,
	TargetGroup targetGroup
) {}