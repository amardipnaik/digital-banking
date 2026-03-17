package com.company.digital.account.repository;

import com.company.digital.account.entity.AccountStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountStatusHistoryRepository extends JpaRepository<AccountStatusHistory, Long> {
	Page<AccountStatusHistory> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
}

