package com.beautiflow.chat.dto;

import com.beautiflow.chat.domain.MessageTemplate;
import com.beautiflow.chat.domain.SendTiming;
import com.beautiflow.chat.domain.TargetGroup;

public record MessageTemplateRes(
	Long id,
	String name,
	SendTiming sendTiming,
	int daysOffset,
	String content,
	TargetGroup targetGroup,
	boolean isActive
) {
	public static MessageTemplateRes from(MessageTemplate template) {
		return new MessageTemplateRes(
			template.getId(),
			template.getName(),
			template.getSendTiming(),
			template.getDaysOffset(),
			template.getContent(),
			template.getTargetGroup(),
			template.isActive()
		);
	}
}