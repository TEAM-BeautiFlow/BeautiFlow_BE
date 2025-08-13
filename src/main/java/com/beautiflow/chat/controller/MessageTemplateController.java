package com.beautiflow.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.beautiflow.chat.dto.messageTemplateDto.MessageTemplateCreateReq;
import com.beautiflow.chat.dto.messageTemplateDto.MessageTemplateSummaryRes;
import com.beautiflow.chat.dto.messageTemplateDto.MessageTemplateUpdateReq;
import com.beautiflow.chat.service.MessageTemplateService;
import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.security.authentication.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Message Template", description = "채팅 메시지 템플릿 관리 API")
@RequestMapping("/templates")
public class MessageTemplateController {

	private final MessageTemplateService messageTemplateService;

	@Operation(summary = "메시지 템플릿 생성", description = "디자이너 또는 사장이 메시지 템플릿을 생성합니다.")
	@PostMapping
	public ResponseEntity<ApiResponse<Void>> createTemplate(
		@RequestBody MessageTemplateCreateReq req,
		@AuthenticationPrincipal CustomOAuth2User user
	) {
		messageTemplateService.createTemplate(user.getUserId(), req);
		return ResponseEntity.ok(ApiResponse.successWithNoData());
	}
	@Operation(summary = "메시지 템플릿 수정", description = "기존 템플릿을 수정합니다.")
	@PutMapping("/{templateId}")
	public ResponseEntity<ApiResponse<Void>> updateTemplate(
		@PathVariable Long templateId,
		@RequestBody MessageTemplateUpdateReq req,
		@AuthenticationPrincipal CustomOAuth2User user
	) {
		messageTemplateService.updateTemplate(user.getUserId(), templateId, req);
		return ResponseEntity.ok(ApiResponse.successWithNoData());
	}

	@Operation(summary = "메시지 템플릿 삭제", description = " 템플릿을 삭제합니다.")
	@DeleteMapping("/{templateId}")
	public ResponseEntity<ApiResponse<Void>> deleteTemplate(
		@PathVariable Long templateId,
		@AuthenticationPrincipal CustomOAuth2User user
	) {
		messageTemplateService.deleteTemplate(user.getUserId(), templateId);
		return ResponseEntity.ok(ApiResponse.successWithNoData());
	}

	@Operation(summary = "템플릿 리스트 조회", description = "본인의 템플릿 목록을 조회합니다.")
	@GetMapping
	public ResponseEntity<ApiResponse<List<MessageTemplateSummaryRes>>> getTemplates(
		@AuthenticationPrincipal CustomOAuth2User user
	) {
		List<MessageTemplateSummaryRes> result = messageTemplateService.getTemplates(user.getUserId());
		return ResponseEntity.ok(ApiResponse.success(result));
	}
}