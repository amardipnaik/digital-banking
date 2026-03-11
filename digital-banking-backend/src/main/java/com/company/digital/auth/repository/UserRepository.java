package com.company.digital.auth.repository;

import com.company.digital.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmailIgnoreCase(String email);
	Optional<User> findByMobileNumber(String mobileNumber);
	Optional<User> findByIdAndIsDeletedFalse(Long id);
	boolean existsByEmailIgnoreCase(String email);
	boolean existsByMobileNumber(String mobileNumber);
}
