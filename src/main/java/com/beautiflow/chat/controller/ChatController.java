package com.beautiflow.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.beautiflow.chat.dto.ChatMessageRes;
import com.beautiflow.chat.dto.ChatRoomSummaryRes;
import com.beautiflow.chat.dto.RoomCreateReq;
import com.beautiflow.chat.dto.RoomCreateRes;
import com.beautiflow.chat.service.ChatMessageService;
import com.beautiflow.chat.service.ChatRoomService;
import com.beautiflow.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

	private final ChatRoomService chatRoomService;
	private final ChatMessageService chatMessageService;

	@PostMapping("/rooms")
	@Operation(summary = "채팅방 생성·재입장")
	public ResponseEntity<ApiResponse<RoomCreateRes>> createRoom(
		@RequestBody RoomCreateReq req,
		@RequestParam Long userId) {

		RoomCreateRes res = chatRoomService.createRoom(userId, req);
		return ResponseEntity.ok(ApiResponse.success(res));
	}

	@GetMapping("/rooms")
	@Operation(summary = "채팅방 리스트 조회")
	public ResponseEntity<ApiResponse<List<ChatRoomSummaryRes>>> getMyRooms(
		@RequestParam Long userId
	) {
		List<ChatRoomSummaryRes> rooms = chatRoomService.getMyChatRooms(userId);
		return ResponseEntity.ok(ApiResponse.success(rooms));
	}

	@PatchMapping("/rooms/{roomId}/exit")
	@Operation(summary = "채팅방 나가기")
	public ResponseEntity<ApiResponse<Void>> exitRoom(
		@PathVariable Long roomId,
		@RequestParam Long userId
	) {
		chatRoomService.exitRoom(roomId, userId);
		return ResponseEntity.ok(ApiResponse.successWithNoData());
	}


	@GetMapping("/rooms/{roomId}/messages")
	@Operation(summary = "채팅 메시지 불러오기")
	public ResponseEntity<ApiResponse<List<ChatMessageRes>>> getChatHistory(
		@PathVariable Long roomId,
		@RequestParam Long userId
	) {
		List<ChatMessageRes> messages = chatMessageService.getChatHistory(roomId, userId);
		return ResponseEntity.ok(ApiResponse.success(messages));
	}

}
