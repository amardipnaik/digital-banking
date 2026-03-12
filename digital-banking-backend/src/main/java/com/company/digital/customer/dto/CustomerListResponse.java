package com.company.digital.customer.dto;

import java.util.List;

public record CustomerListResponse(
	List<CustomerListItemResponse> items,
	int page,
	int size,
	long totalElements,
	int totalPages
) {
}

