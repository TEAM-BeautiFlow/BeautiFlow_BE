package com.beautiflow.chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.beautiflow.chat.dto.GroupMessageSendReq;
import com.beautiflow.chat.service.GroupMessageService;
import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.security.authentication.CustomOAuth2User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/group-messages")
@RequiredArgsConstructor
public class GroupMessageController {

	private final GroupMessageService groupMessageService;

	@PostMapping
	public ResponseEntity<ApiResponse<Void>> sendGroupMessage(
		@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
		@RequestBody GroupMessageSendReq req
	) {
		Long designerId = customOAuth2User.getUserId();
		groupMessageService.sendGroupMessage(designerId, req);
		return ResponseEntity.ok(ApiResponse.successWithNoData());
	}
}
