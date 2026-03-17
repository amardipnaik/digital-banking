package com.company.digital.account.dto;

import com.company.digital.account.enums.AccountStatus;
import com.company.digital.account.enums.AccountType;
import java.time.LocalDateTime;

public record AdminAccountListItemResponse(
	Long accountId,
	Long userId,
	String accountNumber,
	String customerName,
	String customerEmail,
	String customerMobile,
	AccountType accountType,
	String currencyCode,
	AccountStatus status,
	LocalDateTime createdAt
) {
}

