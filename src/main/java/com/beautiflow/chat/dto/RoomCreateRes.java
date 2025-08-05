package com.beautiflow.chat.dto;

import com.beautiflow.chat.domain.ChatRoom;

public record RoomCreateRes(
	Long roomId,
	Long shopId,
	String shopName,
	Long customerId,
	Long designerId

) { public static RoomCreateRes of(ChatRoom room) {
	return new RoomCreateRes(
		room.getId(),
		room.getShop().getId(),
		room.getShop().getShopName(),
		room.getCustomer().getId(),
		room.getDesigner().getId()
	);
}
}