package com.company.digital.account.dto;

import com.company.digital.account.enums.AccountStatus;
import com.company.digital.account.enums.AccountType;
import java.time.LocalDateTime;

public record CustomerAccountListItemResponse(
	Long accountId,
	String accountNumber,
	AccountType accountType,
	String currencyCode,
	AccountStatus status,
	LocalDateTime createdAt,
	LocalDateTime openedAt
) {
}

