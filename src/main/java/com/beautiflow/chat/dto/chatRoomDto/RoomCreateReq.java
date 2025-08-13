package com.beautiflow.chat.dto.chatRoomDto;

public record RoomCreateReq(
	Long shopId,
	Long customerId,
	Long designerId
) {}
