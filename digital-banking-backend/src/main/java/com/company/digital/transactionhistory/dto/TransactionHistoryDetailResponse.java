package com.company.digital.transactionhistory.dto;

import com.company.digital.transaction.enums.EntrySide;
import com.company.digital.transaction.enums.TransactionStatus;
import com.company.digital.transaction.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionHistoryDetailResponse(
	String transactionRef,
	Long accountId,
	Long userId,
	TransactionType transactionType,
	EntrySide entrySide,
	BigDecimal amount,
	String currencyCode,
	BigDecimal balanceBefore,
	BigDecimal balanceAfter,
	TransactionStatus status,
	String description,
	String transferGroupRef,
	Long counterpartyAccountId,
	Long createdBy,
	LocalDateTime createdAt,
	String reversalRef
) {
}

