package com.beautiflow.user.repository;

import com.beautiflow.global.domain.GlobalRole;
import com.beautiflow.user.domain.User;
import com.beautiflow.user.domain.UserRole;
import com.beautiflow.user.domain.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    boolean existsByUserAndRole(User user, GlobalRole role);

}
