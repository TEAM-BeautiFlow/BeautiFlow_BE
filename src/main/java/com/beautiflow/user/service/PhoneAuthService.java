package com.beautiflow.user.service;

import java.time.Duration;
import java.util.Random;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.beautiflow.global.common.error.AuthErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.global.common.sms.SmsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PhoneAuthService {

	private final SmsService smsService;
	private final StringRedisTemplate redisTemplate;

	// 인증번호 전송
	public void sendVerificationCode(String phoneNumber) {
		String code = String.valueOf(new Random().nextInt(900000) + 100000); // 6자리

		// Redis 저장 (5분 유효)
		redisTemplate.opsForValue().set("auth-code:" + phoneNumber, code, Duration.ofMinutes(5));

		// SMS 발송
		smsService.sendAuthCode(phoneNumber, code);
	}

	// 인증번호 검증
	public void verifyCode(String phoneNumber, String inputCode) {
		String savedCode = redisTemplate.opsForValue().get("auth-code:" + phoneNumber);
		if (savedCode == null || !savedCode.equals(inputCode)) {
			throw new BeautiFlowException(AuthErrorCode.INVALID_VERIFICATION_CODE);
		}
		redisTemplate.delete("auth-code:" + phoneNumber);
	}
}

