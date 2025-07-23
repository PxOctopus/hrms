package com.cagri.hrms.repository;

import com.cagri.hrms.entity.core.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    int countByCompanyId(Long companyId);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByResetToken(String token);

    // Returns the number of active users (enabled = true) with the role name "MANAGER"
    int countByRoleNameAndEnabledTrue(String roleName);

    List<User> findByPendingCompanyNameIsNotNullAndEnabledFalse();

    List<User> findByRoleNameAndPendingCompanyNameNotNull(String roleName);
}
