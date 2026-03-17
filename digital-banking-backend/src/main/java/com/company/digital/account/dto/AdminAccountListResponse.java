package com.company.digital.account.dto;

import java.util.List;

public record AdminAccountListResponse(
	List<AdminAccountListItemResponse> items,
	int page,
	int size,
	long totalElements,
	int totalPages
) {
}

