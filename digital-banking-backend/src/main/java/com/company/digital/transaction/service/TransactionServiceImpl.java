package com.company.digital.transaction.service;

import com.company.digital.account.entity.Account;
import com.company.digital.account.enums.AccountStatus;
import com.company.digital.account.repository.AccountRepository;
import com.company.digital.auth.entity.User;
import com.company.digital.auth.enums.RoleCode;
import com.company.digital.auth.enums.UserStatus;
import com.company.digital.auth.repository.UserRepository;
import com.company.digital.auth.security.AuthenticatedUser;
import com.company.digital.common.exception.ApiException;
import com.company.digital.transaction.dto.AdminAdjustmentRequest;
import com.company.digital.transaction.dto.AdminReversalRequest;
import com.company.digital.transaction.dto.DepositRequest;
import com.company.digital.transaction.dto.TransactionPostResponse;
import com.company.digital.transaction.dto.TransferRequest;
import com.company.digital.transaction.dto.WithdrawalRequest;
import com.company.digital.transaction.entity.AccountTransaction;
import com.company.digital.transaction.enums.EntrySide;
import com.company.digital.transaction.enums.TransactionStatus;
import com.company.digital.transaction.enums.TransactionType;
import com.company.digital.transaction.repository.AccountTransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

	private static final BigDecimal ZERO = new BigDecimal("0.00");

	private final AccountRepository accountRepository;
	private final AccountTransactionRepository accountTransactionRepository;
	private final UserRepository userRepository;

	public TransactionServiceImpl(
		AccountRepository accountRepository,
		AccountTransactionRepository accountTransactionRepository,
		UserRepository userRepository
	) {
		this.accountRepository = accountRepository;
		this.accountTransactionRepository = accountTransactionRepository;
		this.userRepository = userRepository;
	}

	@Override
	public TransactionPostResponse deposit(DepositRequest request, AuthenticatedUser actor) {
		String idempotencyKey = normalizeNullable(request.idempotencyKey());
		if (idempotencyKey != null) {
			AccountTransaction existing = accountTransactionRepository
				.findFirstByAccountIdAndIdempotencyKeyOrderByCreatedAtDesc(request.accountId(), idempotencyKey)
				.orElse(null);
			if (existing != null) {
				return toSingleResponse(existing, "Duplicate request returned previously posted transaction");
			}
		}

		Account account = lockOwnedActiveCustomerAccount(request.accountId(), actor.userId());
		BigDecimal amount = normalizeAmount(request.amount());
		String currency = resolveCurrency(account, request.currencyCode());
		User initiator = requireUser(actor.userId());

		AccountTransaction posted = postSingle(
			account,
			null,
			TransactionType.DEPOSIT,
			EntrySide.CREDIT,
			amount,
			currency,
			normalizeNullable(request.remarks()),
			idempotencyKey,
			initiator,
			null
		);

		return toSingleResponse(posted, "Deposit posted successfully");
	}

	@Override
	public TransactionPostResponse withdrawal(WithdrawalRequest request, AuthenticatedUser actor) {
		String idempotencyKey = normalizeNullable(request.idempotencyKey());
		if (idempotencyKey != null) {
			AccountTransaction existing = accountTransactionRepository
				.findFirstByAccountIdAndIdempotencyKeyOrderByCreatedAtDesc(request.accountId(), idempotencyKey)
				.orElse(null);
			if (existing != null) {
				return toSingleResponse(existing, "Duplicate request returned previously posted transaction");
			}
		}

		Account account = lockOwnedActiveCustomerAccount(request.accountId(), actor.userId());
		BigDecimal amount = normalizeAmount(request.amount());
		String currency = resolveCurrency(account, request.currencyCode());
		User initiator = requireUser(actor.userId());

		AccountTransaction posted = postSingle(
			account,
			null,
			TransactionType.WITHDRAWAL,
			EntrySide.DEBIT,
			amount,
			currency,
			normalizeNullable(request.remarks()),
			idempotencyKey,
			initiator,
			null
		);

		return toSingleResponse(posted, "Withdrawal posted successfully");
	}

	@Override
	public TransactionPostResponse transfer(TransferRequest request, AuthenticatedUser actor) {
		if (request.sourceAccountId().equals(request.targetAccountId())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "TRANSACTION_INVALID_TRANSFER", "source and target account must be different");
		}

		String idempotencyKey = normalizeNullable(request.idempotencyKey());
		if (idempotencyKey != null) {
			AccountTransaction existingDebit = accountTransactionRepository
				.findFirstByAccountIdAndIdempotencyKeyAndTransactionTypeAndEntrySideOrderByCreatedAtDesc(
					request.sourceAccountId(),
					idempotencyKey,
					TransactionType.TRANSFER,
					EntrySide.DEBIT
				)
				.orElse(null);
			if (existingDebit != null) {
				AccountTransaction existingCredit = existingDebit.getTransferGroupRef() == null
					? null
					: accountTransactionRepository.findFirstByTransferGroupRefAndEntrySideOrderByCreatedAtDesc(existingDebit.getTransferGroupRef(), EntrySide.CREDIT).orElse(null);
				return toTransferResponse(existingDebit, existingCredit, "Duplicate request returned previously posted transfer");
			}
		}

		User initiator = requireUser(actor.userId());
		BigDecimal amount = normalizeAmount(request.amount());

		Long firstId = Math.min(request.sourceAccountId(), request.targetAccountId());
		Long secondId = Math.max(request.sourceAccountId(), request.targetAccountId());
		Account first = lockAccount(firstId);
		Account second = lockAccount(secondId);

		Account source = request.sourceAccountId().equals(first.getId()) ? first : second;
		Account target = request.targetAccountId().equals(first.getId()) ? first : second;

		validateOwnedBy(source, actor.userId());
		validatePostableAccount(source);
		validatePostableAccount(target);
		String sourceCurrency = resolveCurrency(source, request.currencyCode());
		String targetCurrency = resolveCurrency(target, request.currencyCode());
		if (!sourceCurrency.equals(targetCurrency)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "TRANSACTION_INVALID_TRANSFER", "currency mismatch between source and target account");
		}

		String transferGroupRef = "TRF-" + shortRef();
		String remarks = normalizeNullable(request.remarks());
		AccountTransaction debit = postSingle(
			source,
			target,
			TransactionType.TRANSFER,
			EntrySide.DEBIT,
			amount,
			sourceCurrency,
			remarks,
			idempotencyKey,
			initiator,
			transferGroupRef
		);
		AccountTransaction credit = postSingle(
			target,
			source,
			TransactionType.TRANSFER,
			EntrySide.CREDIT,
			amount,
			targetCurrency,
			remarks,
			null,
			initiator,
			transferGroupRef
		);

		return toTransferResponse(debit, credit, "Transfer posted successfully");
	}

	@Override
	public TransactionPostResponse adminAdjustment(AdminAdjustmentRequest request, AuthenticatedUser actor) {
		Account account = lockAccount(request.accountId());
		validateAdjustableAccount(account);

		String idempotencyKey = normalizeNullable(request.idempotencyKey());
		if (idempotencyKey != null) {
			AccountTransaction existing = accountTransactionRepository
				.findFirstByAccountIdAndIdempotencyKeyOrderByCreatedAtDesc(request.accountId(), idempotencyKey)
				.orElse(null);
			if (existing != null) {
				return toSingleResponse(existing, "Duplicate request returned previously posted transaction");
			}
		}

		BigDecimal amount = normalizeAmount(request.amount());
		String currency = resolveCurrency(account, request.currencyCode());
		User initiator = requireUser(actor.userId());
		String reason = normalizeNullable(request.reason());

		AccountTransaction posted = postSingle(
			account,
			null,
			TransactionType.ADJUSTMENT,
			request.entrySide(),
			amount,
			currency,
			reason,
			idempotencyKey,
			initiator,
			null
		);
		return toSingleResponse(posted, "Adjustment posted successfully");
	}

	@Override
	public TransactionPostResponse adminReversal(AdminReversalRequest request, AuthenticatedUser actor) {
		String originalRef = normalizeRequired(request.originalTransactionRef(), "originalTransactionRef");
		AccountTransaction original = accountTransactionRepository.findByTransactionRef(originalRef)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "TRANSACTION_ACCOUNT_NOT_FOUND", "Original transaction not found"));
		if (original.getStatus() == TransactionStatus.REVERSED) {
			throw new ApiException(HttpStatus.CONFLICT, "TRANSACTION_ALREADY_REVERSED", "Transaction is already reversed");
		}

		Account account = lockAccount(original.getAccount().getId());
		validateAdjustableAccount(account);
		User initiator = requireUser(actor.userId());

		EntrySide reversalSide = original.getEntrySide() == EntrySide.DEBIT ? EntrySide.CREDIT : EntrySide.DEBIT;
		AccountTransaction reversal = postSingle(
			account,
			original.getCounterpartyAccount(),
			TransactionType.REVERSAL,
			reversalSide,
			original.getAmount(),
			original.getCurrencyCode(),
			normalizeRequired(request.reason(), "reason"),
			null,
			initiator,
			original.getTransferGroupRef()
		);

		original.setStatus(TransactionStatus.REVERSED);
		accountTransactionRepository.save(original);

		return toSingleResponse(reversal, "Reversal posted successfully");
	}

	private AccountTransaction postSingle(
		Account account,
		Account counterparty,
		TransactionType type,
		EntrySide side,
		BigDecimal amount,
		String currency,
		String description,
		String idempotencyKey,
		User initiator,
		String transferGroupRef
	) {
		BigDecimal before = nonNullBalance(account.getAvailableBalance());
		BigDecimal signedAmount = side == EntrySide.DEBIT ? amount.negate() : amount;
		BigDecimal after = before.add(signedAmount).setScale(2, RoundingMode.HALF_UP);
		if (after.compareTo(ZERO) < 0) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "TRANSACTION_INSUFFICIENT_FUNDS", "Insufficient funds");
		}

		account.setAvailableBalance(after);
		account.setLedgerBalance(after);
		accountRepository.save(account);

		AccountTransaction transaction = new AccountTransaction();
		transaction.setAccount(account);
		transaction.setCounterpartyAccount(counterparty);
		transaction.setTransactionRef("TXN-" + shortRef());
		transaction.setIdempotencyKey(idempotencyKey);
		transaction.setTransactionType(type);
		transaction.setEntrySide(side);
		transaction.setAmount(amount);
		transaction.setCurrencyCode(currency);
		transaction.setBalanceBefore(before);
		transaction.setBalanceAfter(after);
		transaction.setTransferGroupRef(transferGroupRef);
		transaction.setDescription(description);
		transaction.setStatus(TransactionStatus.POSTED);
		transaction.setCreatedBy(initiator);
		return accountTransactionRepository.save(transaction);
	}

	private Account lockOwnedActiveCustomerAccount(Long accountId, Long actorUserId) {
		Account account = accountRepository.findByIdAndUserIdForUpdate(accountId, actorUserId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "TRANSACTION_ACCOUNT_NOT_FOUND", "Account not found"));
		validateOwnedBy(account, actorUserId);
		validatePostableAccount(account);
		return account;
	}

	private Account lockAccount(Long accountId) {
		return accountRepository.findByIdForUpdate(accountId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "TRANSACTION_ACCOUNT_NOT_FOUND", "Account not found"));
	}

	private void validateOwnedBy(Account account, Long actorUserId) {
		if (!account.getUser().getId().equals(actorUserId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "TRANSACTION_ACCOUNT_NOT_ELIGIBLE", "Account is not owned by authenticated customer");
		}
		if (account.getUser().getRole().getCode() != RoleCode.CUSTOMER) {
			throw new ApiException(HttpStatus.FORBIDDEN, "TRANSACTION_ACCOUNT_NOT_ELIGIBLE", "Only customer accounts are eligible");
		}
		if (account.getUser().isDeleted()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "TRANSACTION_ACCOUNT_NOT_ELIGIBLE", "Deleted users cannot post transactions");
		}
		if (account.getUser().getStatus() != UserStatus.ACTIVE) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "TRANSACTION_ACCOUNT_NOT_ELIGIBLE", "User must be active");
		}
	}

	private void validatePostableAccount(Account account) {
		if (account.getStatus() != AccountStatus.ACTIVE) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "TRANSACTION_ACCOUNT_NOT_ELIGIBLE", "Only ACTIVE accounts are eligible for posting");
		}
	}

	private void validateAdjustableAccount(Account account) {
		if (account.getStatus() == AccountStatus.CLOSED) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "TRANSACTION_ACCOUNT_NOT_ELIGIBLE", "CLOSED account cannot be adjusted");
		}
	}

	private User requireUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_USER_NOT_FOUND", "Authenticated user not found"));
	}

	private String resolveCurrency(Account account, String requestedCurrency) {
		String normalized = normalizeNullable(requestedCurrency);
		String currency = normalized == null ? account.getCurrencyCode() : normalized.toUpperCase();
		if (!account.getCurrencyCode().equalsIgnoreCase(currency)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "TRANSACTION_INVALID_TRANSFER", "currency does not match account currency");
		}
		return account.getCurrencyCode();
	}

	private BigDecimal normalizeAmount(BigDecimal amount) {
		if (amount == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "amount is required");
		}
		BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
		if (normalized.compareTo(ZERO) <= 0) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "amount must be greater than 0");
		}
		return normalized;
	}

	private BigDecimal nonNullBalance(BigDecimal value) {
		return value == null ? ZERO : value.setScale(2, RoundingMode.HALF_UP);
	}

	private String normalizeNullable(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private String normalizeRequired(String value, String field) {
		String normalized = normalizeNullable(value);
		if (normalized == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", field + " is required");
		}
		return normalized;
	}

	private String shortRef() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
	}

	private TransactionPostResponse toSingleResponse(AccountTransaction transaction, String message) {
		return new TransactionPostResponse(
			transaction.getTransactionRef(),
			transaction.getTransferGroupRef(),
			null,
			null,
			transaction.getAccount().getId(),
			transaction.getTransactionType() == TransactionType.TRANSFER ? transaction.getAccount().getId() : null,
			transaction.getCounterpartyAccount() != null ? transaction.getCounterpartyAccount().getId() : null,
			transaction.getTransactionType(),
			transaction.getStatus(),
			transaction.getAmount(),
			transaction.getCurrencyCode(),
			transaction.getBalanceAfter(),
			message
		);
	}

	private TransactionPostResponse toTransferResponse(AccountTransaction debit, AccountTransaction credit, String message) {
		return new TransactionPostResponse(
			debit.getTransactionRef(),
			debit.getTransferGroupRef(),
			debit.getTransactionRef(),
			credit != null ? credit.getTransactionRef() : null,
			debit.getAccount().getId(),
			debit.getAccount().getId(),
			debit.getCounterpartyAccount() != null ? debit.getCounterpartyAccount().getId() : null,
			TransactionType.TRANSFER,
			debit.getStatus(),
			debit.getAmount(),
			debit.getCurrencyCode(),
			debit.getBalanceAfter(),
			message
		);
	}
}

