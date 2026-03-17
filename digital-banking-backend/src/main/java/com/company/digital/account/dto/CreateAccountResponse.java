package com.company.digital.account.dto;

import com.company.digital.account.enums.AccountStatus;
import com.company.digital.account.enums.AccountType;

public record CreateAccountResponse(
	Long accountId,
	String accountNumber,
	AccountType accountType,
	String currencyCode,
	AccountStatus status
) {
}

