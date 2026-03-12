package com.company.digital.customer.dto;

import java.util.List;

public record CustomerActivityResponse(
	List<CustomerActivityItemResponse> items,
	int page,
	int size,
	long totalElements,
	int totalPages
) {
}

