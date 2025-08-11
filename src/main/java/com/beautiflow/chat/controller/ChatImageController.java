package com.beautiflow.chat.controller;

import com.beautiflow.chat.domain.ChatRoom;
import com.beautiflow.chat.dto.ChatMessageSendReq;
import com.beautiflow.chat.repository.ChatRoomRepository;
import com.beautiflow.chat.service.ChatMessageService;
import com.beautiflow.chat.service.RedisPubSubService;
import com.beautiflow.global.common.error.ChatRoomErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.s3.S3Service;
import com.beautiflow.global.common.s3.S3UploadResult;
import com.beautiflow.global.common.security.authentication.CustomOAuth2User;
import com.beautiflow.global.common.util.JWTUtil;
import com.beautiflow.global.domain.SenderType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatImageController {

	private final S3Service s3Service;
	private final JWTUtil jwtUtil;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageService chatMessageService;
	private final RedisPubSubService pubSubService;

	@PostMapping(value = "/{roomId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public void uploadImageAndBroadcast(
		@PathVariable Long roomId,
		@RequestPart("file") MultipartFile file,
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User
	) throws Exception {

		Long userId = customOAuth2User.getUserId();
		ChatRoom room = chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new BeautiFlowException(ChatRoomErrorCode.CHATROOM_NOT_FOUND));
		if (!isParticipant(room, userId)) {
			throw new AuthenticationServiceException("채팅방 참가자가 아님");
		}

		S3UploadResult uploaded = s3Service.uploadFile(file, "chat/" + roomId);

		SenderType senderType = resolveSenderType(room, userId);

		ChatMessageSendReq req = new ChatMessageSendReq(
			roomId, userId, senderType, null, uploaded.imageUrl()
		);
		chatMessageService.saveMessage(roomId, req);

		ObjectMapper om = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		pubSubService.publish("chat", om.writeValueAsString(req));
	}

	private boolean isParticipant(ChatRoom room, Long userId) {
		return (room.getCustomer() != null && room.getCustomer().getId().equals(userId)) ||
			(room.getDesigner() != null && room.getDesigner().getId().equals(userId));
	}

	private SenderType resolveSenderType(ChatRoom room, Long userId) {
		if (room.getDesigner() != null && room.getDesigner().getId().equals(userId)) return SenderType.STAFF;
		return SenderType.CUSTOMER;
	}
}
