package com.company.digital.account.dto;

import java.util.List;

public record AccountHistoryResponse(
	List<AccountHistoryItemResponse> items,
	int page,
	int size,
	long totalElements,
	int totalPages
) {
}

