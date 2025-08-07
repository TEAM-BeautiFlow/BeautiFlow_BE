package com.beautiflow.treatment.repository;

import com.beautiflow.treatment.domain.OptionItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionItemRepository extends JpaRepository<OptionItem, Long> {
}

