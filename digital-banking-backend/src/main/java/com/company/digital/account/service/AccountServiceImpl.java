package com.company.digital.account.service;

import com.company.digital.account.dto.AccountDetailResponse;
import com.company.digital.account.dto.AccountHistoryItemResponse;
import com.company.digital.account.dto.AccountHistoryResponse;
import com.company.digital.account.dto.AdminAccountListItemResponse;
import com.company.digital.account.dto.AdminAccountListResponse;
import com.company.digital.account.dto.CreateAccountRequest;
import com.company.digital.account.dto.CreateAccountResponse;
import com.company.digital.account.dto.CustomerAccountListItemResponse;
import com.company.digital.account.dto.CustomerAccountListResponse;
import com.company.digital.account.dto.UpdateAccountStatusRequest;
import com.company.digital.account.entity.Account;
import com.company.digital.account.entity.AccountStatusHistory;
import com.company.digital.account.enums.AccountStatus;
import com.company.digital.account.enums.AccountType;
import com.company.digital.account.repository.AccountRepository;
import com.company.digital.account.repository.AccountStatusHistoryRepository;
import com.company.digital.auth.entity.CustomerProfile;
import com.company.digital.auth.entity.User;
import com.company.digital.auth.enums.RoleCode;
import com.company.digital.auth.enums.UserStatus;
import com.company.digital.auth.repository.CustomerProfileRepository;
import com.company.digital.auth.repository.UserRepository;
import com.company.digital.auth.security.AuthenticatedUser;
import com.company.digital.common.exception.ApiException;
import com.company.digital.customer.enums.KycStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(noRollbackFor = ApiException.class)
public class AccountServiceImpl implements AccountService {

	private final AccountRepository accountRepository;
	private final AccountStatusHistoryRepository accountStatusHistoryRepository;
	private final UserRepository userRepository;
	private final CustomerProfileRepository customerProfileRepository;

	public AccountServiceImpl(
		AccountRepository accountRepository,
		AccountStatusHistoryRepository accountStatusHistoryRepository,
		UserRepository userRepository,
		CustomerProfileRepository customerProfileRepository
	) {
		this.accountRepository = accountRepository;
		this.accountStatusHistoryRepository = accountStatusHistoryRepository;
		this.userRepository = userRepository;
		this.customerProfileRepository = customerProfileRepository;
	}

	@Override
	public CreateAccountResponse createAccount(CreateAccountRequest request, AuthenticatedUser actor) {
		User user = requireCustomerActor(actor.userId());
		CustomerProfile profile = customerProfileRepository.findByUserId(user.getId())
			.orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "ACCOUNT_NOT_ELIGIBLE", "Customer profile not found"));

		validateEligibility(user, profile);

		Account account = new Account();
		account.setUser(user);
		account.setAccountNumber(generateAccountNumber());
		account.setAccountType(request.accountType());
		account.setCurrencyCode(normalizeCurrency(request.currencyCode()));
		account.setStatus(AccountStatus.PENDING_APPROVAL);
		account.setAvailableBalance(BigDecimal.ZERO);
		account.setLedgerBalance(BigDecimal.ZERO);
		account.setCreatedBy(actor.userId());
		account.setUpdatedBy(actor.userId());

		Account saved = accountRepository.save(account);
		appendHistory(saved, null, saved.getStatus(), user, "Account created by customer");

		return new CreateAccountResponse(saved.getId(), saved.getAccountNumber(), saved.getAccountType(), saved.getCurrencyCode(), saved.getStatus());
	}

	@Override
	@Transactional(readOnly = true)
	public CustomerAccountListResponse listMyAccounts(AuthenticatedUser actor, int page, int size, String sort) {
		requireCustomerActor(actor.userId());
		Page<Account> accountPage = accountRepository.findForCustomer(actor.userId(), PageRequest.of(Math.max(page, 0), Math.max(size, 1), parseSort(sort)));
		List<CustomerAccountListItemResponse> items = accountPage.getContent().stream().map(this::toCustomerListItem).toList();
		return new CustomerAccountListResponse(items, accountPage.getNumber(), accountPage.getSize(), accountPage.getTotalElements(), accountPage.getTotalPages());
	}

	@Override
	@Transactional(readOnly = true)
	public AccountDetailResponse getMyAccount(Long accountId, AuthenticatedUser actor) {
		requireCustomerActor(actor.userId());
		Account account = accountRepository.findByIdAndUserId(accountId, actor.userId())
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Account not found"));
		return toDetail(account);
	}

	@Override
	@Transactional(readOnly = true)
	public AdminAccountListResponse listAdminAccounts(
		String search,
		AccountStatus status,
		AccountType accountType,
		Long userId,
		int page,
		int size,
		String sort
	) {
		Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), parseSort(sort));
		String normalizedSearch = normalizeForSearch(search);
		Page<Account> accountPage = normalizedSearch == null
			? accountRepository.findForAdminWithoutSearch(status, accountType, userId, pageable)
			: accountRepository.findForAdminWithSearch(normalizedSearch, status, accountType, userId, pageable);

		List<AdminAccountListItemResponse> items = accountPage.getContent().stream().map(this::toAdminListItem).toList();
		return new AdminAccountListResponse(items, accountPage.getNumber(), accountPage.getSize(), accountPage.getTotalElements(), accountPage.getTotalPages());
	}

	@Override
	@Transactional(readOnly = true)
	public AccountDetailResponse getAdminAccount(Long accountId) {
		Account account = accountRepository.findById(accountId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Account not found"));
		return toDetail(account);
	}

	@Override
	public AccountDetailResponse updateAccountStatus(Long accountId, UpdateAccountStatusRequest request, AuthenticatedUser actor) {
		Account account = accountRepository.findById(accountId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Account not found"));
		User changedBy = userRepository.findById(actor.userId())
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_USER_NOT_FOUND", "Authenticated user not found"));

		AccountStatus from = account.getStatus();
		AccountStatus to = request.status();
		validateTransition(from, to);

		account.setStatus(to);
		account.setUpdatedBy(actor.userId());
		if (to == AccountStatus.ACTIVE && account.getOpenedAt() == null) {
			account.setOpenedAt(LocalDateTime.now());
		}
		if (to == AccountStatus.CLOSED) {
			account.setClosedAt(LocalDateTime.now());
			account.setClosedReason(normalizeNullable(request.reason()));
		} else {
			account.setClosedAt(null);
			account.setClosedReason(null);
		}

		Account saved = accountRepository.save(account);
		appendHistory(saved, from, to, changedBy, request.reason());
		return toDetail(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public AccountHistoryResponse getAccountHistory(Long accountId, int page, int size) {
		accountRepository.findById(accountId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Account not found"));
		Page<AccountStatusHistory> historyPage = accountStatusHistoryRepository.findByAccountIdOrderByCreatedAtDesc(
			accountId,
			PageRequest.of(Math.max(page, 0), Math.max(size, 1))
		);
		List<AccountHistoryItemResponse> items = historyPage.getContent().stream().map(item -> new AccountHistoryItemResponse(
			item.getId(),
			item.getFromStatus(),
			item.getToStatus(),
			item.getChangedBy().getId(),
			item.getReason(),
			item.getCreatedAt()
		)).toList();
		return new AccountHistoryResponse(items, historyPage.getNumber(), historyPage.getSize(), historyPage.getTotalElements(), historyPage.getTotalPages());
	}

	private User requireCustomerActor(Long userId) {
		User user = userRepository.findByIdAndRoleCodeAndIsDeletedFalse(userId, RoleCode.CUSTOMER)
			.orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_FORBIDDEN", "Only customer users can perform this operation"));
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "ACCOUNT_NOT_ELIGIBLE", "Customer account is not active");
		}
		return user;
	}

	private void validateEligibility(User user, CustomerProfile profile) {
		if (user.isDeleted()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "ACCOUNT_NOT_ELIGIBLE", "Deleted users cannot create account");
		}
		if (profile.getKycStatus() != KycStatus.APPROVED) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "ACCOUNT_NOT_ELIGIBLE", "KYC must be approved to create account");
		}
	}

	private void validateTransition(AccountStatus from, AccountStatus to) {
		if (from == to) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "ACCOUNT_INVALID_TRANSITION", "Account is already in requested status");
		}

		boolean allowed = switch (from) {
			case PENDING_APPROVAL -> to == AccountStatus.ACTIVE || to == AccountStatus.CLOSED;
			case ACTIVE -> to == AccountStatus.FROZEN || to == AccountStatus.CLOSED;
			case FROZEN -> to == AccountStatus.ACTIVE || to == AccountStatus.CLOSED;
			case CLOSED -> false;
		};
		if (!allowed) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "ACCOUNT_INVALID_TRANSITION", "Requested status transition is not allowed");
		}
	}

	private void appendHistory(Account account, AccountStatus from, AccountStatus to, User changedBy, String reason) {
		AccountStatusHistory history = new AccountStatusHistory();
		history.setAccount(account);
		history.setFromStatus(from);
		history.setToStatus(to);
		history.setChangedBy(changedBy);
		history.setReason(normalizeNullable(reason));
		history.setMetadata("accountNumber=" + account.getAccountNumber());
		accountStatusHistoryRepository.save(history);
	}

	private String generateAccountNumber() {
		for (int i = 0; i < 10; i++) {
			long numeric = ThreadLocalRandom.current().nextLong(1_000_000_000_000L, 9_999_999_999_999L);
			String candidate = Long.toString(numeric);
			if (!accountRepository.existsByAccountNumber(candidate)) {
				return candidate;
			}
		}
		throw new ApiException(HttpStatus.CONFLICT, "ACCOUNT_NUMBER_CONFLICT", "Unable to generate a unique account number");
	}

	private String normalizeCurrency(String value) {
		String normalized = normalizeNullable(value);
		return normalized == null ? "INR" : normalized.toUpperCase(Locale.ROOT);
	}

	private String normalizeNullable(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private String normalizeForSearch(String value) {
		String normalized = normalizeNullable(value);
		return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
	}

	private Sort parseSort(String sortValue) {
		if (sortValue == null || sortValue.isBlank()) {
			return Sort.by(Sort.Direction.DESC, "createdAt");
		}
		String[] parts = sortValue.split(",");
		String field = parts[0].trim();
		Sort.Direction direction = parts.length > 1 ? Sort.Direction.fromOptionalString(parts[1].trim()).orElse(Sort.Direction.DESC) : Sort.Direction.DESC;
		String mappedField = switch (field) {
			case "createdAt" -> "createdAt";
			case "status" -> "status";
			case "accountType" -> "accountType";
			case "accountNumber" -> "accountNumber";
			default -> "createdAt";
		};
		return Sort.by(direction, mappedField);
	}

	private CustomerAccountListItemResponse toCustomerListItem(Account account) {
		return new CustomerAccountListItemResponse(
			account.getId(),
			account.getAccountNumber(),
			account.getAccountType(),
			account.getCurrencyCode(),
			account.getStatus(),
			account.getCreatedAt(),
			account.getOpenedAt()
		);
	}

	private AdminAccountListItemResponse toAdminListItem(Account account) {
		CustomerProfile profile = customerProfileRepository.findByUserId(account.getUser().getId()).orElse(null);
		return new AdminAccountListItemResponse(
			account.getId(),
			account.getUser().getId(),
			account.getAccountNumber(),
			profile != null ? profile.getFullName() : null,
			account.getUser().getEmail(),
			account.getUser().getMobileNumber(),
			account.getAccountType(),
			account.getCurrencyCode(),
			account.getStatus(),
			account.getCreatedAt()
		);
	}

	private AccountDetailResponse toDetail(Account account) {
		CustomerProfile profile = customerProfileRepository.findByUserId(account.getUser().getId()).orElse(null);
		return new AccountDetailResponse(
			account.getId(),
			account.getUser().getId(),
			account.getAccountNumber(),
			account.getAccountType(),
			account.getCurrencyCode(),
			account.getStatus(),
			profile != null ? profile.getFullName() : null,
			account.getUser().getEmail(),
			account.getUser().getMobileNumber(),
			account.getCreatedAt(),
			account.getOpenedAt(),
			account.getClosedAt(),
			account.getClosedReason()
		);
	}
}

