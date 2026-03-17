package com.company.digital.balance.dto;

import com.company.digital.transaction.enums.EntrySide;
import com.company.digital.transaction.enums.TransactionStatus;
import com.company.digital.transaction.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MiniStatementItemResponse(
	String transactionRef,
	TransactionType transactionType,
	EntrySide entrySide,
	BigDecimal amount,
	String currencyCode,
	TransactionStatus status,
	LocalDateTime createdAt,
	String description
) {
}

