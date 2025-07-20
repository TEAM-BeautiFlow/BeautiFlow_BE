package com.beautiflow.global.common.Alert;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beautiflow.chat.dto.ChatMessageSendReq;
import com.beautiflow.chat.repository.ChatRoomReadRepository;
import com.beautiflow.chat.service.ChatMessageService;
import com.beautiflow.chat.service.RedisPubSubService;
import com.beautiflow.global.common.sms.SmsService;
import com.beautiflow.global.domain.SenderType;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlertEventHandler {

	private final SmsService smsService;
	private final ChatMessageService chatMessageService;
	private final ChatRoomReadRepository chatRoomReadRepository;
	private final UserRepository userRepository;
	private final RedisPubSubService pubSubService;

	private final ObjectMapper objectMapper = new ObjectMapper()
		.registerModule(new JavaTimeModule())
		.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


	@Transactional
	public void handle(AlertEvent event) {
		User receiver = userRepository.findById(event.getReceiverId())
			.orElseThrow(() -> new RuntimeException("User not found"));

		if (event.getType().equals("SMS")) {
			smsService.sendReminderTo(receiver.getContact());
		} else if (event.getType().equals("TEMPLATE")) {
			ChatMessageSendReq sendReq = new ChatMessageSendReq(
				event.getChatRoomId(),
				event.getSenderId(),
				SenderType.STAFF,
				event.getMessageContent(),
				null
			);
			chatMessageService.saveMessage(event.getChatRoomId(), sendReq);

			try {
				String msg = objectMapper.writeValueAsString(sendReq);
				pubSubService.publish("chat", msg);
			} catch (JsonProcessingException e) {
				throw new RuntimeException("Failed to serialize chat message", e);
			}
		}

		chatRoomReadRepository.findByChatRoomIdAndUserId(event.getChatRoomId(), event.getReceiverId())
			.ifPresent(read -> {
				read.updateLastAlertSentTime(LocalDateTime.now());
				chatRoomReadRepository.save(read);
			});
	}
}
