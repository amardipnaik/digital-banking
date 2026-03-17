package com.company.digital.transactionhistory.controller;

import com.company.digital.auth.security.AuthenticatedUser;
import com.company.digital.common.api.ApiResponse;
import com.company.digital.common.exception.ApiException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions/history")
@Tag(name = "Transaction History", description = "Customer transaction history APIs")
public class TransactionHistoryController {

	private final TransactionHistoryService transactionHistoryService;

	public TransactionHistoryController(TransactionHistoryService transactionHistoryService) {
		this.transactionHistoryService = transactionHistoryService;
	}

	@GetMapping
	@Operation(summary = "List my transaction history", description = "Returns paginated transaction history for authenticated customer")
	public ResponseEntity<ApiResponse<TransactionHistoryListResponse>> list(
		@RequestParam(required = false) Long accountId,
		@RequestParam(required = false) TransactionType type,
		@RequestParam(required = false) EntrySide entrySide,
		@RequestParam(required = false) TransactionStatus status,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(defaultValue = "createdAt,desc") String sort,
		Authentication authentication
	) {
		AuthenticatedUser actor = getAuthenticatedUser(authentication);
		return ResponseEntity.ok(ApiResponse.success(
			transactionHistoryService.listMyHistory(actor, accountId, type, entrySide, status, from, to, page, size, sort)
		));
	}

	@GetMapping("/{transactionRef}")
	@Operation(summary = "Get my transaction detail", description = "Returns transaction detail if it belongs to authenticated customer")
	public ResponseEntity<ApiResponse<TransactionHistoryDetailResponse>> detail(
		@PathVariable String transactionRef,
		Authentication authentication
	) {
		AuthenticatedUser actor = getAuthenticatedUser(authentication);
		return ResponseEntity.ok(ApiResponse.success(transactionHistoryService.getMyTransactionDetail(actor, transactionRef)));
	}

	private AuthenticatedUser getAuthenticatedUser(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHORIZED", "User is not authenticated");
		}
		return user;
	}
}

