package com.beautiflow.chat.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

import com.beautiflow.chat.service.ChatRoomService;
import com.beautiflow.global.common.util.JWTUtil;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompHandler implements ChannelInterceptor {


	private final JWTUtil jwtUtill;
	private final ChatRoomService chatRoomService;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		if (StompCommand.CONNECT == accessor.getCommand()) {
			String token = extractToken(accessor);
			try {
				if (jwtUtill.isExpired(token)) {
					throw new JwtException("token expired");
				}
				log.info("CONNECT -JWT 유효성 통과");
			} catch (Exception e) {
				throw new AuthenticationServiceException("JWT 인증 실패: " + e.getMessage());
			}
		}

		if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
			log.info("SUBSCRIBE 요청 - destination: {}", accessor.getDestination());

			String token = extractToken(accessor);
			String kakaoId = jwtUtill.getKakaoId(token);
			Long userId = jwtUtill.getUserId(token);
			String destination = accessor.getDestination();
			if (destination == null || !destination.startsWith("/topic/")) {
				log.error("잘못된 destination: {}", destination);
				throw new AuthenticationServiceException("잘못된 destination입니다.");
			}

			try {
				String[] parts = destination.split("/");
				Long roomId = Long.parseLong(parts[parts.length - 1]);

				if (!chatRoomService.isParticipant(userId, roomId)) {
					log.warn("해당 채팅방 접근 권한 없음: userId={} roomId={}", userId, roomId);
					throw new AuthenticationServiceException("채팅방 참가자가 아님");
				}
				log.info("채팅방 권한 확인 완료 - userId={} roomId={}", userId, roomId);
			} catch (NumberFormatException e) {
				throw new AuthenticationServiceException("roomId 형식 오류");
			}
		}

		return message;
	}

	private String extractToken(StompHeaderAccessor accessor) {
		String bearerToken = accessor.getFirstNativeHeader("Authorization");
		if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
			throw new AuthenticationServiceException("Authorization 헤더가 잘못되었습니다");
		}
		return bearerToken.substring(7).trim();
	}
}