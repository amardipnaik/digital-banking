package com.company.digital.transaction.repository;

import com.company.digital.transaction.entity.AccountTransaction;
import com.company.digital.transaction.enums.EntrySide;
import com.company.digital.transaction.enums.TransactionStatus;
import com.company.digital.transaction.enums.TransactionType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {
	Optional<AccountTransaction> findFirstByAccountIdAndIdempotencyKeyOrderByCreatedAtDesc(Long accountId, String idempotencyKey);

	Optional<AccountTransaction> findFirstByAccountIdAndIdempotencyKeyAndTransactionTypeAndEntrySideOrderByCreatedAtDesc(
		Long accountId,
		String idempotencyKey,
		TransactionType transactionType,
		EntrySide entrySide
	);

	Optional<AccountTransaction> findByTransactionRef(String transactionRef);

	Optional<AccountTransaction> findFirstByTransferGroupRefAndEntrySideOrderByCreatedAtDesc(String transferGroupRef, EntrySide entrySide);

	Optional<AccountTransaction> findFirstByAccountIdAndTransactionTypeAndAmountAndCreatedAtGreaterThanOrderByCreatedAtAsc(
		Long accountId,
		TransactionType transactionType,
		java.math.BigDecimal amount,
		LocalDateTime createdAt
	);

	Page<AccountTransaction> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

	@Query(
		"""
			select t
			from AccountTransaction t
			join t.account a
			where a.user.id = :userId
			and (:accountId is null or a.id = :accountId)
			and (:type is null or t.transactionType = :type)
			and (:entrySide is null or t.entrySide = :entrySide)
			and (:status is null or t.status = :status)
			and t.createdAt >= :fromTs
			and t.createdAt <= :toTs
		"""
	)
	Page<AccountTransaction> findHistoryForCustomer(
		@Param("userId") Long userId,
		@Param("accountId") Long accountId,
		@Param("type") TransactionType type,
		@Param("entrySide") EntrySide entrySide,
		@Param("status") TransactionStatus status,
		@Param("fromTs") LocalDateTime fromTs,
		@Param("toTs") LocalDateTime toTs,
		Pageable pageable
	);

	@Query(
		"""
			select t
			from AccountTransaction t
			join t.account a
			where (:accountId is null or a.id = :accountId)
			and (:userId is null or a.user.id = :userId)
			and (:type is null or t.transactionType = :type)
			and (:entrySide is null or t.entrySide = :entrySide)
			and (:status is null or t.status = :status)
			and t.createdAt >= :fromTs
			and t.createdAt <= :toTs
		"""
	)
	Page<AccountTransaction> findHistoryForAdmin(
		@Param("accountId") Long accountId,
		@Param("userId") Long userId,
		@Param("type") TransactionType type,
		@Param("entrySide") EntrySide entrySide,
		@Param("status") TransactionStatus status,
		@Param("fromTs") LocalDateTime fromTs,
		@Param("toTs") LocalDateTime toTs,
		Pageable pageable
	);
}

