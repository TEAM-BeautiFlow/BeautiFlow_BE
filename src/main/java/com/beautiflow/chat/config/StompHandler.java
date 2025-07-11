package com.beautiflow.chat.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompHandler implements ChannelInterceptor {

	//TODO 로그인 구현 전이므로 jwtUtil은 주석 처리했슴돠
	// private final JwtUtill jwtUtill;
	//private final ChatRoomService chatRoomService;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		if (StompCommand.CONNECT == accessor.getCommand()) {
			log.info("CONNECT 요청 수신 (임시 인증 로직 생략)");
			// 임시로 토큰 인증 생략
			// String token = extractToken(accessor);
			// jwtUtill.isExpired(token);
		}

		if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
			log.info("SUBSCRIBE 진입 (임시 인증 로직 생략)");
			log.info("Destination: {}", accessor.getDestination());

			String destination = accessor.getDestination();
			if (destination == null || !destination.startsWith("/topic/")) {
				log.error("잘못된 destination: {}", destination);
				throw new AuthenticationServiceException("잘못된 destination입니다.");
			}

			String[] parts = destination.split("/");
			if (parts.length < 3) {
				log.error("destination split 오류: {}", (Object) parts);
				throw new AuthenticationServiceException("destination 파싱 오류");
			}

			String roomIdStr = parts[parts.length - 1];
			try {
				Long roomId = Long.parseLong(roomIdStr);
				log.info("Room ID: {}", roomId);

				//TODO 임시 사용자 email (실제 로그인 구현 후 제거)
				/*String dummyEmail = "temp@user.com";

				if (!chatRoomService.isRoomParticipant(dummyEmail, roomId)) {
					log.warn("Room 참가자 아님: {} not in room {}", dummyEmail, roomId);
					throw new AuthenticationServiceException("해당 room에 권한이 없습니다");
				}*/
			} catch (NumberFormatException e) {
				log.error("roomId가 숫자 아님: {}", parts[2]);
				throw new AuthenticationServiceException("roomId가 숫자가 아님");
			}
		}

		return message;
	}

	//TODO 토큰 추출 메서드는 주석 처리
	//	private String extractToken(StompHeaderAccessor accessor) {
	//		String bearerToken = accessor.getFirstNativeHeader("Authorization");
	//		if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
	//			throw new AuthenticationServiceException("Authorization 헤더가 잘못되었습니다");
	//		}
	//		return bearerToken.substring(7).trim();
	//	}
}
