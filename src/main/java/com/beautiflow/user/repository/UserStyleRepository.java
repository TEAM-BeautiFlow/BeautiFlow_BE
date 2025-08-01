package com.beautiflow.user.repository;

import com.beautiflow.user.domain.UserStyle;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStyleRepository extends JpaRepository<UserStyle,Long> {

    Optional<UserStyle> findByUserId(Long userId);
}
