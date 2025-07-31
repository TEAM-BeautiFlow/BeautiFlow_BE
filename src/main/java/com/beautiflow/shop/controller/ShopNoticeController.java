package com.beautiflow.shop.controller;

import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.shop.domain.ShopNotice;
import com.beautiflow.shop.dto.NoticeCreateReq;
import com.beautiflow.shop.dto.NoticeUpdateReq;
import com.beautiflow.shop.dto.ShopNoticeRes;
import com.beautiflow.shop.service.ShopNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shops/{shopId}/notices")
public class ShopNoticeController {

  private final ShopNoticeService shopNoticeService;

  // 매장 공지사항 조회
  @Operation(summary = "매장 공지사항 조회")
  @GetMapping
  public ResponseEntity<ApiResponse<List<ShopNoticeRes>>> getNotices(
      @PathVariable Long shopId
  ) {
    List<ShopNoticeRes> notices = shopNoticeService.getNotices(shopId);
    return ResponseEntity.ok(ApiResponse.success(notices));
  }

  // 매장 공지사항 등록
  @Operation(summary = "매장 공지사항 등록")
  @PostMapping
  public ResponseEntity<ApiResponse<ShopNotice>> createNotice(
      @PathVariable Long shopId,
      @RequestBody NoticeCreateReq requestDto
  ) {
    ShopNotice createdNotice = shopNoticeService.createNotice(shopId, requestDto);

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdNotice));
  }

  // 매장 공지사항 수정
  @Operation(summary = "매장 공지사항 수정")
  @PatchMapping("/{noticeId}")
  public ResponseEntity<ApiResponse<ShopNotice>> updateNotice(
      @PathVariable Long shopId,
      @PathVariable Long noticeId,
      @RequestBody NoticeUpdateReq requestDto
  ) {
    ShopNotice updatedNotice = shopNoticeService.updateNotice(shopId, noticeId, requestDto);
    return ResponseEntity.ok(ApiResponse.success(updatedNotice));
  }

  // 매장 공지사항 삭제
  @Operation(summary = "매장 공지사항 삭제")
  @DeleteMapping("/{noticeId}")
  public ResponseEntity<ApiResponse<Void>> deleteNotice(
      @PathVariable Long shopId,
      @PathVariable Long noticeId
  ) {
    shopNoticeService.deleteNotice(shopId, noticeId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }


}
