package com.beautiflow.reservation.dto.response;

import java.util.List;

public record PaymentInfoRes(
        List<String> namePath,
        Integer price
) {

}
