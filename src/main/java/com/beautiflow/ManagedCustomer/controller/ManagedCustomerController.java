package com.beautiflow.ManagedCustomer.controller;

import com.beautiflow.ManagedCustomer.dto.CustomerDetailRes;
import com.beautiflow.ManagedCustomer.dto.CustomerListSimpleRes;
import com.beautiflow.ManagedCustomer.dto.CustomerReservationItem;
import com.beautiflow.ManagedCustomer.dto.CustomerUpdateReq;
import com.beautiflow.ManagedCustomer.dto.CustomerUpdateRes;
import com.beautiflow.ManagedCustomer.service.ManagedCustomerService;
import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.security.authentication.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mangedCustomer")
public class ManagedCustomerController {

  private final ManagedCustomerService managedCustomerService;

  @GetMapping("/{customerId}")
  @Operation(summary = "고객 상세 정보 조회", description = "디자이너가 관리 중인 특정 고객의 상세 정보를 조회합니다.")
  public ResponseEntity<ApiResponse<CustomerDetailRes>> getCustomerDetail(
      @AuthenticationPrincipal CustomOAuth2User user,
      @PathVariable Long customerId
  ) {
    CustomerDetailRes result = managedCustomerService.getCustomerDetail(user.getUserId(), customerId);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @PatchMapping("/{customerId}")
  @Operation(summary = "고객 정보 수정 (메모/그룹)", description = "디자이너가 관리 중인 특정 고객의 메모와 그룹을 수정합니다.")
  public ResponseEntity<ApiResponse<CustomerUpdateRes>> updateCustomer(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable Long customerId,
      @RequestBody @Valid CustomerUpdateReq req
  ) {
    CustomerUpdateRes result = managedCustomerService.updateCustomerInfo(customOAuth2User.getUserId(), customerId, req);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @GetMapping("/list")
  @Operation(summary = "고객 그룹 목록 조회", description = "디자이너가 관리 중인 고객을 그룹 필터링 기준으로 조회합니다.")
  public ResponseEntity<ApiResponse<List<CustomerListSimpleRes>>> getCustomersByGroup(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @RequestParam(required = false) List<String> groups
  ) {
    List<CustomerListSimpleRes> result = managedCustomerService.getCustomersByGroup(customOAuth2User.getUserId(), groups);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @GetMapping("/{customerId}/reservations")
  @Operation(summary = "고객 시술 내역 조회", description = "특정 고객의 모든 예약(시술) 내역을 상태값 포함해 조회합니다.")
  public ResponseEntity<ApiResponse<List<CustomerReservationItem>>> getCustomerReservationHistory(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable Long customerId
  ) {
    List<CustomerReservationItem> result =
        managedCustomerService.getCustomerReservationHistory(customOAuth2User.getUserId(), customerId);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @DeleteMapping("/{customerId}")
  @Operation(summary = "고객 삭제", description = "디자이너가 관리 중인 특정 고객을 삭제합니다.")
  public ResponseEntity<ApiResponse<Void>> deleteCustomer(
      @AuthenticationPrincipal CustomOAuth2User user,
      @PathVariable Long customerId
  ) {
    managedCustomerService.deleteCustomer(user.getUserId(), customerId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }



}
