package com.beautiflow.reservation.repository;

import com.beautiflow.treatment.domain.TreatmentImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TreatmentImageRepository extends JpaRepository<TreatmentImage, Long> {
}
