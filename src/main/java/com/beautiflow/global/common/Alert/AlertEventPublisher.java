package com.beautiflow.global.common.Alert;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertEventPublisher {
	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;

	public void publish(AlertEvent event) {
		try {
			String json = objectMapper.writeValueAsString(event);
			log.info("🔔 AlertEvent 전송: {}", json);
			redisTemplate.convertAndSend("alertQueue", json);
		} catch (Exception e) {

		}
	}
}