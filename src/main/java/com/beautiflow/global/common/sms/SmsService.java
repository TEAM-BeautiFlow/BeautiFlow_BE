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
	private String normalizedFrom;

	@PostConstruct
	public void init() {
		this.messageService = NurigoApp.INSTANCE.initialize(
			smsProperties.getApiKey(),
			smsProperties.getApiSecret(),
			"https://api.solapi.com"		);
		this.normalizedFrom = digitsOnly(smsProperties.getFromNumber());
		if (this.normalizedFrom == null || this.normalizedFrom.isBlank()) {
			log.warn("발신번호(from-number)가 유효하지 않습니다. yml을 확인하세요. from={}", smsProperties.getFromNumber());
		}
	}
	private static String digitsOnly(String raw) {
		if (raw == null) return null;
		String d = raw.replaceAll("\\D+", "");
		if (d.isEmpty()) return null;
		if (d.startsWith("82")) {
			String rest = d.substring(2);
			if (!rest.startsWith("0")) rest = "0" + rest;
			return rest;
		}
		return d;
	}

	private void send(String to, String text) {
		String normalizedTo = PhoneNormalizerKR.toE164(to);
		if (normalizedFrom == null || normalizedTo == null) {
			log.warn("발신/수신 번호가 유효하지 않아 전송 취소. from={}, to={}", masked(normalizedFrom), masked(normalizedTo));
			return;
		}
		try {
			Message message = new Message();
			message.setFrom(normalizedFrom);
			message.setTo(normalizedTo);
			message.setText(text);
			SingleMessageSentResponse res = messageService.sendOne(new SingleMessageSendingRequest(message));
			log.info("SMS 전송 성공: {}", res.getMessageId());
		} catch (Exception e) {
			log.error("SMS 전송 실패: {}", e.getMessage(), e);
		}
	}

	private String masked(String v) {
		if (v == null) return null;
		if (v.length() <= 4) return "****";
		return v.substring(0, v.length()-4).replaceAll("\\d", "*") + v.substring(v.length()-4);
	}

	public void sendNewContactAlert(String toPhoneNumber) {
		send(toPhoneNumber, "새로운 문의를 시작했습니다. BeautiFlow에서 확인해주세요.");
	}

	public void sendAuthCode(String toPhoneNumber, String code) {
		send(toPhoneNumber, "[BeautiFlow] 인증번호: " + code);
	}

	public void sendUnreadReminder(String toPhoneNumber, String shopName, String roomDeepLink) {
		String body = "[BeautiFlow] " + shopName +
			"에서 새로운 메시지가 1시간 이상 확인되지 않았습니다.\n" +
			"바로 확인: " + roomDeepLink;
		send(toPhoneNumber, body);
	}
}
