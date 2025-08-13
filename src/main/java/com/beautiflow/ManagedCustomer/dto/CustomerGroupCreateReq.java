package com.beautiflow.ManagedCustomer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerGroupCreateReq(
    @NotBlank
    @Size(min = 1, max = 30)
    @Pattern(regexp = "^[a-zA-Z0-9가-힣_-]{1,30}$",
        message = "code는 한글/영문/숫자/대시/언더스코어만 허용")
    String code
) {}