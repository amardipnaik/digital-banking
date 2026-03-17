package com.company.digital.account.service;

import com.company.digital.account.dto.AccountDetailResponse;
import com.company.digital.account.dto.AccountHistoryResponse;
import com.company.digital.account.dto.AdminAccountListResponse;
import com.company.digital.account.dto.CreateAccountRequest;
import com.company.digital.account.dto.CreateAccountResponse;
import com.company.digital.account.dto.CustomerAccountListResponse;
import com.company.digital.account.dto.UpdateAccountStatusRequest;
import com.company.digital.account.enums.AccountStatus;
import com.company.digital.account.enums.AccountType;
import com.company.digital.auth.security.AuthenticatedUser;

public interface AccountService {
	CreateAccountResponse createAccount(CreateAccountRequest request, AuthenticatedUser actor);

	CustomerAccountListResponse listMyAccounts(AuthenticatedUser actor, int page, int size, String sort);

	AccountDetailResponse getMyAccount(Long accountId, AuthenticatedUser actor);

	AdminAccountListResponse listAdminAccounts(String search, AccountStatus status, AccountType accountType, Long userId, int page, int size, String sort);

	AccountDetailResponse getAdminAccount(Long accountId);

	AccountDetailResponse updateAccountStatus(Long accountId, UpdateAccountStatusRequest request, AuthenticatedUser actor);

	AccountHistoryResponse getAccountHistory(Long accountId, int page, int size);
}

