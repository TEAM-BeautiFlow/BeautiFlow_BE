package com.beautiflow.MangedCustomer.controller;

import com.beautiflow.MangedCustomer.dto.CustomerDetailRes;
import com.beautiflow.MangedCustomer.service.ManagedCustomerService;
import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.security.authentication.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/MangedCustomer")
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
}
