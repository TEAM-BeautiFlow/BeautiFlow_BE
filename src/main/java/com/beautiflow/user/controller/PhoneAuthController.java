package com.beautiflow.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.user.dto.PhoneAuthReq;
import com.beautiflow.user.dto.VerifyCodeReq;
import com.beautiflow.user.service.PhoneAuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/phone")
@RequiredArgsConstructor
public class PhoneAuthController {

	private final PhoneAuthService phoneAuthService;

	@PostMapping("/send-code")
	public ResponseEntity<ApiResponse<String>> sendCode(@RequestBody PhoneAuthReq request) {
		phoneAuthService.sendVerificationCode(request.phoneNumber());
		return ResponseEntity.ok(ApiResponse.success(request.phoneNumber()));
	}

	@PostMapping("/verify-code")
	public ResponseEntity<ApiResponse<Void>> verifyCode(@RequestBody VerifyCodeReq request) {
		phoneAuthService.verifyCode(request.phoneNumber(), request.code());
		return ResponseEntity.ok(ApiResponse.successWithNoData());
	}
}
