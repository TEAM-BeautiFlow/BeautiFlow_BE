package com.beautiflow.ManagedCustomer.dto;

import com.beautiflow.ManagedCustomer.domain.ManagedCustomer;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.domain.UserStyle;
import com.beautiflow.user.domain.UserStyleImage;
import java.util.List;

public record CustomerDetailRes(
    Long customerId,
    String name,
    String contact,
    String email,            // 추가
    List<String> styleImageUrls,
    String requestNotes,     // 여기로 스타일 설명 이동
    String memo              // 사장님 개인 메모
) {
  public static CustomerDetailRes from(ManagedCustomer mc) {
    User customer = mc.getCustomer();
    UserStyle style = customer.getStyle();

    String styleDesc = (style != null) ? style.getDescription() : null; // 요청사항 값
    List<String> styleImageUrls = (style != null && style.getImages() != null)
        ? style.getImages().stream().map(UserStyleImage::getImageUrl).toList()
        : List.of();

    return new CustomerDetailRes(
        customer.getId(),
        customer.getName(),
        customer.getContact(),
        customer.getEmail(),   //  User.email 가져옴         // description은 비워둠
        styleImageUrls,
        styleDesc,              // 요청사항에 스타일 설명
        mc.getMemo()            // 사장님 메모
    );
  }
}
