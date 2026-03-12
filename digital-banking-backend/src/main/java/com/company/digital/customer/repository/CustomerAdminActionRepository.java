package com.company.digital.customer.repository;

import com.company.digital.customer.entity.CustomerAdminAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAdminActionRepository extends JpaRepository<CustomerAdminAction, Long> {
	Page<CustomerAdminAction> findByCustomerUserIdOrderByCreatedAtDesc(Long customerUserId, Pageable pageable);
}

