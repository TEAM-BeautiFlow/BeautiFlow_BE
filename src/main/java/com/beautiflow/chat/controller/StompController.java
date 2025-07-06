package com.beautiflow.chat.controller;

import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import com.beautiflow.chat.dto.ChatMessageSendReq;
import com.beautiflow.chat.service.ChatMessageService;
import com.beautiflow.chat.service.RedisPubSubService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class StompController {

	private final ChatMessageService chatMessageService;
	private final RedisPubSubService pubSubService;
	private final SimpMessageSendingOperations messageTemplate;

	@MessageMapping("/{roomId}")
	public void sendMessage(@DestinationVariable Long roomId, ChatMessageSendReq chatMessageSendReq) throws
		JsonProcessingException {
		chatMessageService.saveMessage(roomId, chatMessageSendReq);

		ChatMessageSendReq enriched = new ChatMessageSendReq(
			chatMessageSendReq.roomId(),
			chatMessageSendReq.senderId(),
			chatMessageSendReq.senderType(),
			chatMessageSendReq.content(),
			chatMessageSendReq.imageUrl()
		);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		String message = objectMapper.writeValueAsString(enriched);
		pubSubService.publish("chat", message);
	}
}
