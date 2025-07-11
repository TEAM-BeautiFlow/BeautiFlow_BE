package com.beautiflow.global.common.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatRoomErrorCode implements ErrorCode{


	CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATROOM404", "채팅방를 찾을 수 없습니다."),
	CHAT_ROOM_ALREADY_EXISTS(HttpStatus.FORBIDDEN, "CHATROOM403", "이미 존재하는 방입니다"),
	INVALID_CHATROOM_PARAMETER(HttpStatus.BAD_REQUEST, "CHATROOM400", "채팅방 요청 데이터가 올바르지 않습니다.");


	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
