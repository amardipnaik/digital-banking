package com.company.digital.balance.dto;

import com.company.digital.account.enums.AccountStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BalanceSummaryResponse(
	Long accountId,
	String accountNumber,
	String currencyCode,
	AccountStatus accountStatus,
	BigDecimal availableBalance,
	BigDecimal ledgerBalance,
	LocalDateTime asOf
) {
}

