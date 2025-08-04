package com.beautiflow.MangedCustomer.controller;
import com.beautiflow.MangedCustomer.dto.CustomerUpdateReq;
import com.beautiflow.MangedCustomer.dto.CustomerUpdateRes;
import com.beautiflow.MangedCustomer.service.ManagedCustomerService;
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
@RequestMapping("/mangedCustomer")
public class ManagedCustomerController {

  private final ManagedCustomerService managedCustomerService;


  @PatchMapping("/{customerId}")
  @Operation(summary = "고객 정보 수정 (메모/그룹)")
  public ResponseEntity<ApiResponse<CustomerUpdateRes>> updateCustomer(
      @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
      @PathVariable Long customerId,
      @RequestBody @Valid CustomerUpdateReq req
  ) {
    CustomerUpdateRes result = managedCustomerService.updateCustomerInfo(customOAuth2User.getUserId(), customerId, req);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

}
