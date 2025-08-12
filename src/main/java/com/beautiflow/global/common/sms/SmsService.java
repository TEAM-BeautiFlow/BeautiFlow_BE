package com.beautiflow.global.common.sms;

import org.springframework.stereotype.Service;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;

import net.nurigo.sdk.message.model.Message;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

	private final SmsProperties smsProperties;
	private DefaultMessageService messageService;

	@PostConstruct
	public void init() {
		this.messageService = NurigoApp.INSTANCE.initialize(
			smsProperties.getApiKey(),
			smsProperties.getApiSecret(),
			"https://api.solapi.com"		);
	}

	public void sendNewContactAlert(String toPhoneNumber) {
		try {
			Message message = new Message();
			message.setFrom(smsProperties.getFromNumber());
			message.setTo(toPhoneNumber);
			message.setText("새로운 문의를 시작했습니다. BeautiFlow에서 확인해주세요.");

			SingleMessageSentResponse response = messageService.sendOne(new SingleMessageSendingRequest(message));
			log.info("📤 SMS 발송 성공: {}", response.getMessageId());
		} catch (Exception e) {
			log.error("❌ SMS 발송 실패: {}", e.getMessage(), e);
		}
	}

	public void sendAuthCode(String toPhoneNumber, String code) {
		try {
			Message message = new Message();
			message.setFrom(smsProperties.getFromNumber());
			message.setTo(toPhoneNumber);
			message.setText("[BeautiFlow] 인증번호: " + code);

			SingleMessageSentResponse response = messageService.sendOne(new SingleMessageSendingRequest(message));
			log.info("📤 인증번호 발송 성공: {}", response.getMessageId());
		} catch (Exception e) {
			log.error("❌ 인증번호 발송 실패: {}", e.getMessage(), e);
		}
	}
}
