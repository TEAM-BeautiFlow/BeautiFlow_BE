package com.beautiflow.ManagedCustomer.controller;

import com.beautiflow.ManagedCustomer.dto.CustomerGroupCreateReq;
import com.beautiflow.ManagedCustomer.dto.CustomerGroupRes;
import com.beautiflow.ManagedCustomer.service.ManagedCustomerService;
import com.beautiflow.global.common.ApiResponse;
import com.beautiflow.global.common.security.authentication.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customer-groups")
public class CustomerGroupController {

  private final ManagedCustomerService service;

  @PostMapping
  @Operation(summary = "커스텀 고객 그룹 추가", description = "code만 받습니다.")
  public ResponseEntity<ApiResponse<CustomerGroupRes>> create(
      @AuthenticationPrincipal CustomOAuth2User principal,
      @RequestBody @Valid CustomerGroupCreateReq req
  ) {
    CustomerGroupRes result = service.create(principal.getUserId(), req);
    return ResponseEntity.ok(ApiResponse.success(result));
  }
  }