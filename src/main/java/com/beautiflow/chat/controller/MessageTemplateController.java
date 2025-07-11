package com.beautiflow.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.beautiflow.chat.dto.MessageTemplateCreateReq;
import com.beautiflow.chat.dto.MessageTemplateRes;
import com.beautiflow.chat.dto.MessageTemplateUpdateReq;
import com.beautiflow.chat.service.MessageTemplateService;
import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.security.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Message Template", description = "템플릿 생성 및 조회 API")
@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class MessageTemplateController {

	private final MessageTemplateService templateService;

	@Operation(summary = "템플릿 생성", description = "디자이너 또는 사장이 템플릿을 생성합니다.")
	@PostMapping
	public ResponseEntity<ApiResponse<Void>> createTemplate(@RequestBody MessageTemplateCreateReq req, @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
		templateService.create(customOAuth2User.getUserId(), req);
		return ResponseEntity.ok(ApiResponse.successWithNoData());
	}

	@Operation(summary = "내 템플릿 목록 조회", description = "현재 로그인된 사용자의 템플릿 목록을 조회합니다.")
	@GetMapping
	public ResponseEntity<ApiResponse<List<MessageTemplateRes>>> getMyTemplates(@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
		return ResponseEntity.ok(ApiResponse.success(templateService.getMyTemplates(customOAuth2User.getUserId())));
	}

	@Operation(summary = "템플릿 수정")
	@PutMapping("/{templateId}")
	public ResponseEntity<ApiResponse<Void>> updateTemplate(
		@PathVariable Long templateId,
		@RequestBody MessageTemplateUpdateReq req) {
		templateService.update(templateId, req);
		return ResponseEntity.ok(ApiResponse.successWithNoData());
	}

	@Operation(summary = "템플릿 삭제 (영구)")
	@DeleteMapping("/{templateId}")
	public ResponseEntity<ApiResponse<Void>> deleteTemplate(
		@PathVariable Long templateId) {
		templateService.delete(templateId);
		return ResponseEntity.ok(ApiResponse.successWithNoData());
	}
}
