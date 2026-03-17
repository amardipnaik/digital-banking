package com.company.digital.transactionhistory.dto;

import com.company.digital.transaction.enums.EntrySide;
import com.company.digital.transaction.enums.TransactionStatus;
import com.company.digital.transaction.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionHistoryListItemResponse(
	String transactionRef,
	Long accountId,
	TransactionType transactionType,
	EntrySide entrySide,
	BigDecimal amount,
	String currencyCode,
	TransactionStatus status,
	BigDecimal balanceAfter,
	String transferGroupRef,
	Long counterpartyAccountId,
	LocalDateTime createdAt
) {
}

