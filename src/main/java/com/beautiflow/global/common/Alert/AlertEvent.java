package com.beautiflow.global.common.Alert;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AlertEvent {
	private Long chatRoomId;
	private Long receiverId;
	private Long senderId;
	private String type; // e.g., "SMS" or "TEMPLATE"
	private String messageContent;

}