package com.beautiflow.customer.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class DesignerCustomerId implements Serializable {
  private Long designerId;
  private Long userId;
  private Long shopId;
}
