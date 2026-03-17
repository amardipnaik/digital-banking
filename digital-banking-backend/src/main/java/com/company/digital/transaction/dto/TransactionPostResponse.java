package com.company.digital.transaction.dto;

import com.company.digital.transaction.enums.TransactionStatus;
import com.company.digital.transaction.enums.TransactionType;
import java.math.BigDecimal;

public record TransactionPostResponse(
	String transactionRef,
	String transferGroupRef,
	String debitTransactionRef,
	String creditTransactionRef,
	Long accountId,
	Long sourceAccountId,
	Long targetAccountId,
	TransactionType type,
	TransactionStatus status,
	BigDecimal amount,
	String currencyCode,
	BigDecimal balanceAfter,
	String message
) {
}

