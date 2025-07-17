package com.beautiflow.treatment.domain.service;

import com.beautiflow.global.domain.TreatmentCategory;
import com.beautiflow.shop.dto.TreatmentInfoRes;
import com.beautiflow.treatment.domain.Treatment;
import com.beautiflow.treatment.domain.repository.TreatmentRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TreatmentService {

  private final TreatmentRepository treatmentRepository;

  public List<TreatmentInfoRes> getTreatments(Long shopId, TreatmentCategory category) {

    List<Treatment> treatments;
    treatments = treatmentRepository.findByShopIdAndCategory(shopId, category);

    // 조회된 엔티티 리스트를 DTO 리스트로 변환
    return treatments.stream()
        .map(TreatmentInfoRes::from)
        .collect(Collectors.toList());
  }
}
