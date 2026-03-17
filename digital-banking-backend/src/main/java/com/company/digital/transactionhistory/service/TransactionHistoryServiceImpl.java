package com.company.digital.transactionhistory.service;

import com.company.digital.account.entity.Account;
import com.company.digital.account.repository.AccountRepository;
import com.company.digital.auth.security.AuthenticatedUser;
import com.company.digital.common.exception.ApiException;
import com.company.digital.transaction.entity.AccountTransaction;
import com.company.digital.transaction.enums.EntrySide;
import com.company.digital.transaction.enums.TransactionStatus;
import com.company.digital.transaction.enums.TransactionType;
import com.company.digital.transaction.repository.AccountTransactionRepository;
import com.company.digital.transactionhistory.dto.TransactionHistoryDetailResponse;
import com.company.digital.transactionhistory.dto.TransactionHistoryListItemResponse;
import com.company.digital.transactionhistory.dto.TransactionHistoryListResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TransactionHistoryServiceImpl implements TransactionHistoryService {

	private static final LocalDateTime MIN_HISTORY_TS = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
	private static final LocalDateTime MAX_HISTORY_TS = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

	private final AccountRepository accountRepository;
	private final AccountTransactionRepository accountTransactionRepository;

	public TransactionHistoryServiceImpl(AccountRepository accountRepository, AccountTransactionRepository accountTransactionRepository) {
		this.accountRepository = accountRepository;
		this.accountTransactionRepository = accountTransactionRepository;
	}

	@Override
	public TransactionHistoryListResponse listMyHistory(
		AuthenticatedUser actor,
		Long accountId,
		TransactionType type,
		EntrySide entrySide,
		TransactionStatus status,
		LocalDateTime from,
		LocalDateTime to,
		int page,
		int size,
		String sort
	) {
		validateDateRange(from, to);
		if (accountId != null) {
			accountRepository.findByIdAndUserId(accountId, actor.userId())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "HISTORY_ACCOUNT_NOT_FOUND", "Account not found"));
		}
		LocalDateTime effectiveFrom = from != null ? from : MIN_HISTORY_TS;
		LocalDateTime effectiveTo = to != null ? to : MAX_HISTORY_TS;

		Page<AccountTransaction> historyPage = accountTransactionRepository.findHistoryForCustomer(
			actor.userId(),
			accountId,
			type,
			entrySide,
			status,
			effectiveFrom,
			effectiveTo,
			PageRequest.of(Math.max(page, 0), Math.max(size, 1), parseSort(sort))
		);

		return toPageResponse(historyPage);
	}

	@Override
	public TransactionHistoryDetailResponse getMyTransactionDetail(AuthenticatedUser actor, String transactionRef) {
		AccountTransaction transaction = accountTransactionRepository.findByTransactionRef(transactionRef)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "HISTORY_TRANSACTION_NOT_FOUND", "Transaction not found"));
		if (!transaction.getAccount().getUser().getId().equals(actor.userId())) {
			throw new ApiException(HttpStatus.FORBIDDEN, "HISTORY_ACCOUNT_FORBIDDEN", "Transaction does not belong to authenticated user");
		}
		return toDetail(transaction);
	}

	@Override
	public TransactionHistoryListResponse listAdminHistory(
		Long accountId,
		Long userId,
		TransactionType type,
		EntrySide entrySide,
		TransactionStatus status,
		LocalDateTime from,
		LocalDateTime to,
		int page,
		int size,
		String sort
	) {
		validateDateRange(from, to);
		if (accountId != null) {
			accountRepository.findById(accountId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "HISTORY_ACCOUNT_NOT_FOUND", "Account not found"));
		}
		LocalDateTime effectiveFrom = from != null ? from : MIN_HISTORY_TS;
		LocalDateTime effectiveTo = to != null ? to : MAX_HISTORY_TS;
		Page<AccountTransaction> historyPage = accountTransactionRepository.findHistoryForAdmin(
			accountId,
			userId,
			type,
			entrySide,
			status,
			effectiveFrom,
			effectiveTo,
			PageRequest.of(Math.max(page, 0), Math.max(size, 1), parseSort(sort))
		);

		return toPageResponse(historyPage);
	}

	@Override
	public TransactionHistoryDetailResponse getAdminTransactionDetail(String transactionRef) {
		AccountTransaction transaction = accountTransactionRepository.findByTransactionRef(transactionRef)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "HISTORY_TRANSACTION_NOT_FOUND", "Transaction not found"));
		return toDetail(transaction);
	}

	private void validateDateRange(LocalDateTime from, LocalDateTime to) {
		if (from != null && to != null && from.isAfter(to)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "HISTORY_INVALID_FILTER", "from date must be before or equal to to date");
		}
	}

	private Sort parseSort(String sortValue) {
		if (sortValue == null || sortValue.isBlank()) {
			return Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
		}
		String[] parts = sortValue.split(",");
		String field = parts[0].trim();
		Sort.Direction direction = parts.length > 1 ? Sort.Direction.fromOptionalString(parts[1].trim()).orElse(Sort.Direction.DESC) : Sort.Direction.DESC;
		String mappedField = switch (field) {
			case "amount" -> "amount";
			case "transactionType" -> "transactionType";
			case "createdAt" -> "createdAt";
			default -> "createdAt";
		};
		Sort base = Sort.by(direction, mappedField);
		return "createdAt".equals(mappedField) ? base.and(Sort.by(Sort.Direction.DESC, "id")) : base;
	}

	private TransactionHistoryListResponse toPageResponse(Page<AccountTransaction> historyPage) {
		List<TransactionHistoryListItemResponse> items = historyPage.getContent().stream().map(this::toListItem).toList();
		return new TransactionHistoryListResponse(items, historyPage.getNumber(), historyPage.getSize(), historyPage.getTotalElements(), historyPage.getTotalPages());
	}

	private TransactionHistoryListItemResponse toListItem(AccountTransaction transaction) {
		return new TransactionHistoryListItemResponse(
			transaction.getTransactionRef(),
			transaction.getAccount().getId(),
			transaction.getTransactionType(),
			transaction.getEntrySide(),
			transaction.getAmount(),
			transaction.getCurrencyCode(),
			transaction.getStatus(),
			transaction.getBalanceAfter(),
			transaction.getTransferGroupRef(),
			transaction.getCounterpartyAccount() != null ? transaction.getCounterpartyAccount().getId() : null,
			transaction.getCreatedAt()
		);
	}

	private TransactionHistoryDetailResponse toDetail(AccountTransaction transaction) {
		String reversalRef = transaction.getStatus() == TransactionStatus.REVERSED
			? accountTransactionRepository
				.findFirstByAccountIdAndTransactionTypeAndAmountAndCreatedAtGreaterThanOrderByCreatedAtAsc(
					transaction.getAccount().getId(),
					TransactionType.REVERSAL,
					transaction.getAmount(),
					transaction.getCreatedAt()
				)
				.map(AccountTransaction::getTransactionRef)
				.orElse(null)
			: null;

		return new TransactionHistoryDetailResponse(
			transaction.getTransactionRef(),
			transaction.getAccount().getId(),
			transaction.getAccount().getUser().getId(),
			transaction.getTransactionType(),
			transaction.getEntrySide(),
			transaction.getAmount(),
			transaction.getCurrencyCode(),
			transaction.getBalanceBefore(),
			transaction.getBalanceAfter(),
			transaction.getStatus(),
			transaction.getDescription(),
			transaction.getTransferGroupRef(),
			transaction.getCounterpartyAccount() != null ? transaction.getCounterpartyAccount().getId() : null,
			transaction.getCreatedBy().getId(),
			transaction.getCreatedAt(),
			reversalRef
		);
	}
}

