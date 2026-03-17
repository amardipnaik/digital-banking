package com.company.digital.balance.dto;

import java.util.List;

public record MiniStatementResponse(
	Long accountId,
	int limit,
	List<MiniStatementItemResponse> items
) {
}

