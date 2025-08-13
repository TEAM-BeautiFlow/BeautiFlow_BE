package com.beautiflow.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.beautiflow.chat.dto.chatMessageDto.ChatMessageRes;
import com.beautiflow.chat.dto.chatRoomDto.ChatRoomSummaryRes;
import com.beautiflow.chat.dto.chatRoomDto.RoomCreateReq;
import com.beautiflow.chat.dto.chatRoomDto.RoomCreateRes;
import com.beautiflow.chat.service.ChatMessageService;
import com.beautiflow.chat.service.ChatRoomService;
import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.security.authentication.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Chat", description = "채팅 관련 API")
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

	private final ChatRoomService chatRoomService;
	private final ChatMessageService chatMessageService;

	@PostMapping("/rooms")
	@Operation(summary = "채팅방 생성·재입장")
	public ResponseEntity<ApiResponse<RoomCreateRes>> createRoom(
		@RequestBody RoomCreateReq req,
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {

		RoomCreateRes res = chatRoomService.createRoom(customOAuth2User.getUserId(), req);
		return ResponseEntity.ok(ApiResponse.success(res));
	}

	@GetMapping("/rooms")
	@Operation(summary = "채팅방 리스트 조회")
	public ResponseEntity<ApiResponse<List<ChatRoomSummaryRes>>> getMyRooms(
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User
	) {
		List<ChatRoomSummaryRes> rooms = chatRoomService.getMyChatRooms(customOAuth2User.getUserId());
		return ResponseEntity.ok(ApiResponse.success(rooms));
	}

	@PatchMapping("/rooms/{roomId}/exit")
	@Operation(summary = "채팅방 나가기")
	public ResponseEntity<ApiResponse<Void>> exitRoom(
		@PathVariable Long roomId,
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User
	) {
		chatRoomService.exitRoom(roomId, customOAuth2User.getUserId());
		return ResponseEntity.ok(ApiResponse.successWithNoData());
	}


	@GetMapping("/rooms/{roomId}/messages")
	@Operation(summary = "채팅 메시지 불러오기")
	public ResponseEntity<ApiResponse<List<ChatMessageRes>>> getChatHistory(
		@PathVariable Long roomId,
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User
	) {
		List<ChatMessageRes> messages = chatMessageService.getChatHistory(roomId, customOAuth2User.getUserId());
		return ResponseEntity.ok(ApiResponse.success(messages));
	}

}