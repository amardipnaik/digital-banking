package com.company.digital.account.dto;

import java.util.List;

public record CustomerAccountListResponse(
	List<CustomerAccountListItemResponse> items,
	int page,
	int size,
	long totalElements,
	int totalPages
) {
}

