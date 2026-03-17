package com.company.digital.account.dto;

import com.company.digital.account.enums.AccountStatus;
import com.company.digital.account.enums.AccountType;
import java.time.LocalDateTime;

public record AccountDetailResponse(
	Long accountId,
	Long userId,
	String accountNumber,
	AccountType accountType,
	String currencyCode,
	AccountStatus status,
	String customerName,
	String customerEmail,
	String customerMobile,
	LocalDateTime createdAt,
	LocalDateTime openedAt,
	LocalDateTime closedAt,
	String closedReason
) {
}

