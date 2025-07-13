package com.beautiflow.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.beautiflow.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long Id);

    Optional<User> findByKakaoId(String kakaoId);
}
