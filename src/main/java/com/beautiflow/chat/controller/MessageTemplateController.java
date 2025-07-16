package com.beautiflow.chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.beautiflow.chat.dto.MessageTemplateCreateReq;
import com.beautiflow.chat.service.MessageTemplateService;
import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.security.CustomOAuth2User;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/templates")
public class MessageTemplateController {

	private final MessageTemplateService messageTemplateService;

	@PostMapping
	public ResponseEntity<ApiResponse<Void>> createTemplate(
		@RequestBody MessageTemplateCreateReq req,
		@AuthenticationPrincipal CustomOAuth2User user
	) {
		messageTemplateService.createTemplate(user.getUserId(), req);
		return ResponseEntity.ok(ApiResponse.successWithNoData());
	}
}