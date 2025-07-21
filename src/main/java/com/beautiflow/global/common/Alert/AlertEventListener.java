package com.beautiflow.global.common.Alert;

import java.nio.charset.StandardCharsets;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AlertEventListener implements MessageListener {

	private final ObjectMapper objectMapper;
	private final AlertEventHandler alertEventHandler;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			String json = new String(message.getBody(), StandardCharsets.UTF_8);
			AlertEvent event = objectMapper.readValue(json, AlertEvent.class);
			alertEventHandler.handle(event);
		} catch (Exception e) {
			// 로깅 및 에러 처리
		}
	}
}
