package com.beautiflow.MangedCustomer.dto;

import com.beautiflow.MangedCustomer.domain.ManagedCustomer;
import com.beautiflow.reservation.domain.Reservation;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.domain.UserStyle;
import com.beautiflow.user.domain.UserStyleImage;
import java.util.Comparator;
import java.util.List;

public record CustomerDetailRes(
    Long customerId,
    String name,
    String contact,
    String description,
    List<String> styleImageUrls,
    String requestNotes,
    String memo
) {
  public static CustomerDetailRes from(ManagedCustomer mc) {
    User customer = mc.getCustomer();
    UserStyle style = customer.getStyle();

    String description = style != null ? style.getDescription() : null;
    List<String> styleImageUrls = style != null
        ? style.getImages().stream().map(UserStyleImage::getImageUrl).toList()
        : List.of();

    String latestRequestNotes = customer.getReservations().stream()
        .max(Comparator.comparing(Reservation::getReservationDate))
        .map(Reservation::getRequestNotes)
        .orElse(null);

    return new CustomerDetailRes(
        customer.getId(),
        customer.getName(),
        customer.getContact(),
        description,
        styleImageUrls,
        latestRequestNotes,
        mc.getMemo()
    );
  }
}

