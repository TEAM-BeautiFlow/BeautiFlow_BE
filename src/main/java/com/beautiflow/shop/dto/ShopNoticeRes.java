package com.beautiflow.shop.dto;

import com.beautiflow.shop.domain.ShopNotice;
import lombok.Getter;

@Getter
public class ShopNoticeRes {
  private final Long noticeId;
  private final String title;
  private final String content;

  public ShopNoticeRes(ShopNotice notice) {
    this.noticeId = notice.getId();
    this.title = notice.getTitle();
    this.content = notice.getContent();
  }
}
