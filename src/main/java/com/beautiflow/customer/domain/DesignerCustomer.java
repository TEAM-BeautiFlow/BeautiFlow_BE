package com.beautiflow.customer.domain;

import com.beautiflow.shop.domain.Shop;
import com.beautiflow.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "designer_customers")
public class DesignerCustomer {

  @EmbeddedId
  private DesignerCustomerId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("designerId")
  private User designer;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("userId")
  private User customer;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("shopId")
  private Shop shop;
}
