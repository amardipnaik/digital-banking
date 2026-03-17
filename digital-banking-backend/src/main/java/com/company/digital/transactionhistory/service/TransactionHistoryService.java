package com.company.digital.transactionhistory.service;

import com.company.digital.auth.security.AuthenticatedUser;
import com.company.digital.transaction.enums.EntrySide;
import com.company.digital.transaction.enums.TransactionStatus;
import com.company.digital.transaction.enums.TransactionType;
import com.company.digital.transactionhistory.dto.TransactionHistoryDetailResponse;
import com.company.digital.transactionhistory.dto.TransactionHistoryListResponse;
import java.time.LocalDateTime;

public interface TransactionHistoryService {
	TransactionHistoryListResponse listMyHistory(
		AuthenticatedUser actor,
		Long accountId,
		TransactionType type,
		EntrySide entrySide,
		TransactionStatus status,
		LocalDateTime from,
		LocalDateTime to,
		int page,
		int size,
		String sort
	);

	TransactionHistoryDetailResponse getMyTransactionDetail(AuthenticatedUser actor, String transactionRef);

	TransactionHistoryListResponse listAdminHistory(
		Long accountId,
		Long userId,
		TransactionType type,
		EntrySide entrySide,
		TransactionStatus status,
		LocalDateTime from,
		LocalDateTime to,
		int page,
		int size,
		String sort
	);

	TransactionHistoryDetailResponse getAdminTransactionDetail(String transactionRef);
}

