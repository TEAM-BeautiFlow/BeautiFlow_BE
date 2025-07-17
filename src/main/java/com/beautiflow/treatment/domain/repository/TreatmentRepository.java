package com.beautiflow.treatment.domain.repository;

import com.beautiflow.global.domain.TreatmentCategory;
import com.beautiflow.treatment.domain.Treatment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

  // 카테고리 필터링
  List<Treatment> findByShopIdAndCategory(Long shopId, TreatmentCategory category);
}
