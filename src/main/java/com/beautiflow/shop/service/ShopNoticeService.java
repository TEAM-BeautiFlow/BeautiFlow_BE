package com.beautiflow.shop.service;

import com.beautiflow.global.common.error.ShopErrorCode;
import com.beautiflow.global.common.exception.BeautiFlowException;
import com.beautiflow.shop.domain.Shop;
import com.beautiflow.shop.domain.ShopNotice;
import com.beautiflow.shop.dto.NoticeCreateReq;
import com.beautiflow.shop.dto.NoticeUpdateReq;
import com.beautiflow.shop.dto.ShopNoticeRes;
import com.beautiflow.shop.repository.ShopNoticeRepository;
import com.beautiflow.shop.repository.ShopRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopNoticeService {

  private final ShopRepository shopRepository;
  private final ShopNoticeRepository shopNoticeRepository;

  // 매장 공지 조회
  @Transactional(readOnly = true)
  public List<ShopNoticeRes> getNotices(Long shopId) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

    return shop.getNotices().stream()
        .map(ShopNoticeRes::new)
        .collect(Collectors.toList());
  }

  // 매장 공지 생성
  @Transactional
  public ShopNotice createNotice(Long shopId, NoticeCreateReq requestDto) {
    Shop shop = shopRepository.findById(shopId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND));

    ShopNotice notice = ShopNotice.builder()
        .title(requestDto.title())
        .content(requestDto.content())
        .shop(shop)
        .build();

    return shopNoticeRepository.save(notice);
  }

  // 매장 공지 수정
  @Transactional
  public ShopNotice updateNotice(Long shopId, Long noticeId, NoticeUpdateReq requestDto) {
    if (!shopRepository.existsById(shopId)) {
      throw new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND);
    }

    ShopNotice notice = shopNoticeRepository.findById(noticeId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.NOTICE_NOT_FOUND));

    if (!notice.getShop().getId().equals(shopId)) {
      throw new BeautiFlowException(ShopErrorCode.FORBIDDEN_NOTICE_ACCESS);
    }

    notice.update(requestDto.title(), requestDto.content());

    return notice;
  }

  // 매장 공지 삭제
  @Transactional
  public void deleteNotice(Long shopId, Long noticeId) {
    if (!shopRepository.existsById(shopId)) {
      throw new BeautiFlowException(ShopErrorCode.SHOP_NOT_FOUND);
    }

    ShopNotice notice = shopNoticeRepository.findById(noticeId)
        .orElseThrow(() -> new BeautiFlowException(ShopErrorCode.NOTICE_NOT_FOUND));

    if (!notice.getShop().getId().equals(shopId)) {
      throw new BeautiFlowException(ShopErrorCode.FORBIDDEN_NOTICE_ACCESS);
    }

    shopNoticeRepository.delete(notice);
  }
}
