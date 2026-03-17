package com.company.digital.account.dto;

import com.company.digital.account.enums.AccountStatus;
import java.time.LocalDateTime;

public record AccountHistoryItemResponse(
	Long id,
	AccountStatus fromStatus,
	AccountStatus toStatus,
	Long changedBy,
	String reason,
	LocalDateTime createdAt
) {
}

