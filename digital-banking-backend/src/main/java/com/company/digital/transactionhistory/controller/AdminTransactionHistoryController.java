package com.company.digital.transactionhistory.controller;

import com.company.digital.common.api.ApiResponse;
import com.company.digital.transaction.enums.EntrySide;
import com.company.digital.transaction.enums.TransactionStatus;
import com.company.digital.transaction.enums.TransactionType;
import com.company.digital.transactionhistory.dto.TransactionHistoryDetailResponse;
import com.company.digital.transactionhistory.dto.TransactionHistoryListResponse;
import com.company.digital.transactionhistory.service.TransactionHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/transactions/history")
@Tag(name = "Admin Transaction History", description = "Admin transaction history APIs")
public class AdminTransactionHistoryController {

	private final TransactionHistoryService transactionHistoryService;

	public AdminTransactionHistoryController(TransactionHistoryService transactionHistoryService) {
		this.transactionHistoryService = transactionHistoryService;
	}

	@GetMapping
	@Operation(summary = "List transaction history", description = "Returns paginated transaction history for admin investigation")
	public ResponseEntity<ApiResponse<TransactionHistoryListResponse>> list(
		@RequestParam(required = false) Long accountId,
		@RequestParam(required = false) Long userId,
		@RequestParam(required = false) TransactionType type,
		@RequestParam(required = false) EntrySide entrySide,
		@RequestParam(required = false) TransactionStatus status,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(defaultValue = "createdAt,desc") String sort
	) {
		return ResponseEntity.ok(ApiResponse.success(
			transactionHistoryService.listAdminHistory(accountId, userId, type, entrySide, status, from, to, page, size, sort)
		));
	}

	@GetMapping("/{transactionRef}")
	@Operation(summary = "Get transaction detail", description = "Returns transaction detail for admin investigation")
	public ResponseEntity<ApiResponse<TransactionHistoryDetailResponse>> detail(@PathVariable String transactionRef) {
		return ResponseEntity.ok(ApiResponse.success(transactionHistoryService.getAdminTransactionDetail(transactionRef)));
	}
}

