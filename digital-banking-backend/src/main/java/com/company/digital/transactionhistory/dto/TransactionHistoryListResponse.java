package com.company.digital.transactionhistory.dto;

import java.util.List;

public record TransactionHistoryListResponse(
	List<TransactionHistoryListItemResponse> items,
	int page,
	int size,
	long totalElements,
	int totalPages
) {
}

