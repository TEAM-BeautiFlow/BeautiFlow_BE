package com.beautiflow.chat.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import com.beautiflow.chat.domain.ChatRoom;
import com.beautiflow.chat.dto.chatMessageDto.ChatMessageSendReq;
import com.beautiflow.chat.repository.ChatRoomRepository;
import com.beautiflow.chat.service.ChatMessageService;
import com.beautiflow.chat.service.RedisPubSubService;
import com.beautiflow.global.common.error.ChatRoomErrorCode;
import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.repository.UserRepository;
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
	private final UserRepository userRepository;
	private final ChatRoomRepository chatRoomRepository;

	@MessageMapping("/{roomId}")
	public void sendMessage(@DestinationVariable Long roomId, ChatMessageSendReq chatMessageSendReq) throws
		JsonProcessingException {
		ChatRoom room = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new BeautiFlowException(ChatRoomErrorCode.CHATROOM_NOT_FOUND));

		User sender = userRepository.findById(chatMessageSendReq.senderId())
			.orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

		// 보낸 사람이 나갔다면 재입장 처리
		room.reEnterBy(sender);

		// 상대방도 나간 상태라면 재입장 처리
		User opponent = sender.equals(room.getCustomer()) ? room.getDesigner() : room.getCustomer();
		room.reEnterBy(opponent);

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
