package com.company.digital.auth.repository;

import com.company.digital.auth.entity.Role;
import com.company.digital.auth.enums.RoleCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
	Optional<Role> findByCode(RoleCode code);
}
