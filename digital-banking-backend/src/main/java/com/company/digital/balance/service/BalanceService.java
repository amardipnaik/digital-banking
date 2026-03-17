package com.company.digital.balance.service;

import com.company.digital.auth.security.AuthenticatedUser;
import com.company.digital.balance.dto.BalanceSummaryResponse;
import com.company.digital.balance.dto.MiniStatementResponse;

public interface BalanceService {
	BalanceSummaryResponse getMyBalance(Long accountId, AuthenticatedUser actor);

	MiniStatementResponse getMyMiniStatement(Long accountId, int limit, AuthenticatedUser actor);

	BalanceSummaryResponse getAdminBalance(Long accountId);

	MiniStatementResponse getAdminMiniStatement(Long accountId, int limit);
}

