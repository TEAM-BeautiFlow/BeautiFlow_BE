package com.beautiflow.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beautiflow.chat.dto.ChatMessageRes;
import com.beautiflow.chat.repository.ChatMessageRepository;
import com.beautiflow.chat.repository.ChatRoomRepository;
import com.beautiflow.chat.domain.ChatMessage;
import com.beautiflow.chat.domain.ChatRoom;
import com.beautiflow.chat.dto.ChatMessageSendReq;
import com.beautiflow.global.common.error.ChatRoomErrorCode;
import com.beautiflow.global.common.error.UserErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

	private final ChatRoomRepository chatRoomRepository;
	private final UserRepository userRepository;
	private final ChatMessageRepository chatMessageRepository;

	public void saveMessage(Long roomId, ChatMessageSendReq req) {
		ChatRoom room = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new BeautiFlowException(ChatRoomErrorCode.CHATROOM_NOT_FOUND));

		User sender = userRepository.findById(req.senderId())
			.orElseThrow(() -> new BeautiFlowException(UserErrorCode.USER_NOT_FOUND));

		if (!room.getCustomer().getId().equals(sender.getId()) && !room.getDesigner().getId().equals(sender.getId())) {
			throw new BeautiFlowException(ChatRoomErrorCode.INVALID_CHATROOM_PARAMETER);
		}

		ChatMessage message = ChatMessage.builder()
			.chatRoom(room)
			.sender(sender)
			.senderType(req.senderType())
			.content(req.content())
			.imageUrl(req.imageUrl())
			.build();

		chatMessageRepository.save(message);
	}

	public List<ChatMessageRes> getChatHistory(Long roomId,Long requesterId){
		ChatRoom room = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new BeautiFlowException(ChatRoomErrorCode.CHATROOM_NOT_FOUND));
		if (!room.getCustomer().getId().equals(requesterId) && !room.getDesigner().getId().equals(requesterId)) {
			throw new BeautiFlowException(ChatRoomErrorCode.INVALID_CHATROOM_PARAMETER);
		}

		return chatMessageRepository.findByChatRoomOrderByCreatedTimeAsc(room).stream()
			.map(msg -> new ChatMessageRes(
				msg.getId(),
				msg.getSender().getId(),
				msg.getSender().getName(),
				msg.getSenderType(),
				msg.getContent(),
				msg.getImageUrl(),
				msg.getCreatedTime()
			))
			.toList();

	}
}