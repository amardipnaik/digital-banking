package com.company.digital.auth.repository;

import com.company.digital.auth.entity.User;
import com.company.digital.auth.enums.RoleCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmailIgnoreCase(String email);
	Optional<User> findByMobileNumber(String mobileNumber);
	Optional<User> findByIdAndIsDeletedFalse(Long id);
	Optional<User> findByIdAndRoleCodeAndIsDeletedFalse(Long id, RoleCode roleCode);
	Optional<User> findByIdAndRoleCode(Long id, RoleCode roleCode);
	boolean existsByEmailIgnoreCase(String email);
	boolean existsByMobileNumber(String mobileNumber);
}
