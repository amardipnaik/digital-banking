package com.company.digital.balance.service;

import com.company.digital.account.entity.Account;
import com.company.digital.account.repository.AccountRepository;
import com.company.digital.auth.security.AuthenticatedUser;
import com.company.digital.balance.dto.BalanceSummaryResponse;
import com.company.digital.balance.dto.MiniStatementItemResponse;
import com.company.digital.balance.dto.MiniStatementResponse;
import com.company.digital.common.exception.ApiException;
import com.company.digital.transaction.entity.AccountTransaction;
import com.company.digital.transaction.repository.AccountTransactionRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BalanceServiceImpl implements BalanceService {

	private static final int MAX_LIMIT = 50;

	private final AccountRepository accountRepository;
	private final AccountTransactionRepository accountTransactionRepository;

	public BalanceServiceImpl(AccountRepository accountRepository, AccountTransactionRepository accountTransactionRepository) {
		this.accountRepository = accountRepository;
		this.accountTransactionRepository = accountTransactionRepository;
	}

	@Override
	public BalanceSummaryResponse getMyBalance(Long accountId, AuthenticatedUser actor) {
		Account account = accountRepository.findByIdAndUserId(accountId, actor.userId())
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BALANCE_ACCOUNT_NOT_FOUND", "Account not found"));
		return toBalance(account);
	}

	@Override
	public MiniStatementResponse getMyMiniStatement(Long accountId, int limit, AuthenticatedUser actor) {
		accountRepository.findByIdAndUserId(accountId, actor.userId())
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BALANCE_ACCOUNT_NOT_FOUND", "Account not found"));
		return toMiniStatement(accountId, limit);
	}

	@Override
	public BalanceSummaryResponse getAdminBalance(Long accountId) {
		Account account = accountRepository.findById(accountId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BALANCE_ACCOUNT_NOT_FOUND", "Account not found"));
		return toBalance(account);
	}

	@Override
	public MiniStatementResponse getAdminMiniStatement(Long accountId, int limit) {
		accountRepository.findById(accountId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "BALANCE_ACCOUNT_NOT_FOUND", "Account not found"));
		return toMiniStatement(accountId, limit);
	}

	private MiniStatementResponse toMiniStatement(Long accountId, int limit) {
		int safeLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
		List<MiniStatementItemResponse> items = accountTransactionRepository
			.findByAccountIdOrderByCreatedAtDesc(accountId, PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt")))
			.getContent()
			.stream()
			.map(this::toMiniItem)
			.toList();
		return new MiniStatementResponse(accountId, safeLimit, items);
	}

	private BalanceSummaryResponse toBalance(Account account) {
		return new BalanceSummaryResponse(
			account.getId(),
			account.getAccountNumber(),
			account.getCurrencyCode(),
			account.getStatus(),
			account.getAvailableBalance(),
			account.getLedgerBalance(),
			account.getUpdatedAt() != null ? account.getUpdatedAt() : account.getCreatedAt()
		);
	}

	private MiniStatementItemResponse toMiniItem(AccountTransaction transaction) {
		return new MiniStatementItemResponse(
			transaction.getTransactionRef(),
			transaction.getTransactionType(),
			transaction.getEntrySide(),
			transaction.getAmount(),
			transaction.getCurrencyCode(),
			transaction.getStatus(),
			transaction.getCreatedAt(),
			transaction.getDescription()
		);
	}
}

