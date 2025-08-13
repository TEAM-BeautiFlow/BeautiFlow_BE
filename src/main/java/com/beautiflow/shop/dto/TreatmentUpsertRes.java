package com.beautiflow.shop.dto;

import com.beautiflow.treatment.domain.Treatment;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TreatmentUpsertRes {
  private Long id;
  private String name;

  public static TreatmentUpsertRes from(Treatment treatment) {
    return new TreatmentUpsertRes(treatment.getId(), treatment.getName());
  }
}
