package com.company.digital.account.repository;

import com.company.digital.account.entity.Account;
import com.company.digital.account.enums.AccountStatus;
import com.company.digital.account.enums.AccountType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long> {
	Optional<Account> findByIdAndUserId(Long id, Long userId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from Account a where a.id = :id")
	Optional<Account> findByIdForUpdate(@Param("id") Long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from Account a where a.id = :id and a.user.id = :userId")
	Optional<Account> findByIdAndUserIdForUpdate(@Param("id") Long id, @Param("userId") Long userId);

	boolean existsByAccountNumber(String accountNumber);

	@Query(
		"""
			select a
			from Account a
			where a.user.id = :userId
		"""
	)
	Page<Account> findForCustomer(@Param("userId") Long userId, Pageable pageable);

	@Query(
		"""
			select a
			from Account a
			join a.user u
			left join CustomerProfile cp on cp.user.id = u.id
			where (:status is null or a.status = :status)
			and (:accountType is null or a.accountType = :accountType)
			and (:userId is null or u.id = :userId)
		"""
	)
	Page<Account> findForAdminWithoutSearch(
		@Param("status") AccountStatus status,
		@Param("accountType") AccountType accountType,
		@Param("userId") Long userId,
		Pageable pageable
	);

	@Query(
		"""
			select a
			from Account a
			join a.user u
			left join CustomerProfile cp on cp.user.id = u.id
			where (
				lower(a.accountNumber) like concat('%', :search, '%')
				or lower(u.email) like concat('%', :search, '%')
				or u.mobileNumber like concat('%', :search, '%')
				or lower(cp.fullName) like concat('%', :search, '%')
			)
			and (:status is null or a.status = :status)
			and (:accountType is null or a.accountType = :accountType)
			and (:userId is null or u.id = :userId)
		"""
	)
	Page<Account> findForAdminWithSearch(
		@Param("search") String search,
		@Param("status") AccountStatus status,
		@Param("accountType") AccountType accountType,
		@Param("userId") Long userId,
		Pageable pageable
	);
}

