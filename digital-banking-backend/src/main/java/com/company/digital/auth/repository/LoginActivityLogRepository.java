package com.company.digital.auth.repository;

import com.company.digital.auth.entity.LoginActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginActivityLogRepository extends JpaRepository<LoginActivityLog, Long> {
}
