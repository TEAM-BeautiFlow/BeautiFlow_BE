package com.beautiflow.chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.beautiflow.chat.dto.chatMessageDto.GroupMessageSendReq;
import com.beautiflow.chat.service.GroupMessageService;
import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.security.authentication.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/group-messages")
@Tag(name = "Group Message", description = "단체 메시지 발송 API")
@RequiredArgsConstructor
public class GroupMessageController {

	private final GroupMessageService groupMessageService;

	@PostMapping
	@Operation(
		summary = "단체 메시지 발송",
		description = "디자이너가 여러 고객에게 단체 메시지를 전송합니다."
	)
	public ResponseEntity<ApiResponse<Void>> sendGroupMessage(
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
		@RequestBody GroupMessageSendReq req
	) {
		Long designerId = customOAuth2User.getUserId();
		groupMessageService.sendGroupMessage(designerId, req);
		return ResponseEntity.ok(ApiResponse.successWithNoData());
	}
}
