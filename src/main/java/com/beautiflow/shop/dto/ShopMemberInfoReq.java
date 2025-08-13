package com.beautiflow.shop.dto;

public record ShopMemberInfoReq(
    String intro,
    Boolean patchImage //이미지 수정 or 삭제 작업 하는 경우 true
) {

}
