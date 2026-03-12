package com.company.digital.customer.controller;

import com.company.digital.auth.dto.MessageResponse;
import com.company.digital.auth.enums.UserStatus;
import com.company.digital.auth.security.AuthenticatedUser;
import com.company.digital.common.api.ApiResponse;
import com.company.digital.common.exception.ApiException;
import com.company.digital.customer.dto.CustomerActivityResponse;
import com.company.digital.customer.dto.CustomerDetailResponse;
import com.company.digital.customer.dto.CustomerListResponse;
import com.company.digital.customer.dto.UpdateCustomerKycRequest;
import com.company.digital.customer.dto.UpdateCustomerProfileRequest;
import com.company.digital.customer.enums.KycStatus;
import com.company.digital.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/customers")
@Tag(name = "Customer Management", description = "Admin-only customer management APIs")
public class CustomerController {

	private final CustomerService customerService;

	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	@GetMapping
	@Operation(summary = "List customers", description = "Returns paginated customers with optional filters")
	public ResponseEntity<ApiResponse<CustomerListResponse>> listCustomers(
		@RequestParam(required = false) String search,
		@RequestParam(required = false) UserStatus userStatus,
		@RequestParam(required = false) KycStatus kycStatus,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "createdAt,desc") String sort
	) {
		return ResponseEntity.ok(ApiResponse.success(customerService.listCustomers(search, userStatus, kycStatus, page, size, sort)));
	}

	@GetMapping("/{userId}")
	@Operation(summary = "Get customer details", description = "Returns customer detail by user id")
	public ResponseEntity<ApiResponse<CustomerDetailResponse>> getCustomer(@PathVariable Long userId) {
		return ResponseEntity.ok(ApiResponse.success(customerService.getCustomer(userId)));
	}

	@PatchMapping("/{userId}")
	@Operation(summary = "Update customer profile", description = "Updates editable customer profile fields")
	public ResponseEntity<ApiResponse<CustomerDetailResponse>> updateProfile(
		@PathVariable Long userId,
		@RequestBody UpdateCustomerProfileRequest request,
		Authentication authentication
	) {
		AuthenticatedUser actor = getAuthenticatedUser(authentication);
		return ResponseEntity.ok(ApiResponse.success(customerService.updateProfile(userId, request, actor)));
	}

	@PatchMapping("/{userId}/kyc")
	@Operation(summary = "Update KYC status", description = "Updates KYC status and remarks for a customer")
	public ResponseEntity<ApiResponse<CustomerDetailResponse>> updateKyc(
		@PathVariable Long userId,
		@Valid @RequestBody UpdateCustomerKycRequest request,
		Authentication authentication
	) {
		AuthenticatedUser actor = getAuthenticatedUser(authentication);
		return ResponseEntity.ok(ApiResponse.success(customerService.updateKyc(userId, request, actor)));
	}

	@DeleteMapping("/{userId}")
	@Operation(summary = "Soft-delete customer", description = "Marks customer user record as deleted")
	public ResponseEntity<ApiResponse<MessageResponse>> softDelete(@PathVariable Long userId, Authentication authentication) {
		AuthenticatedUser actor = getAuthenticatedUser(authentication);
		return ResponseEntity.ok(ApiResponse.success(customerService.softDelete(userId, actor)));
	}

	@PatchMapping("/{userId}/restore")
	@Operation(summary = "Restore customer", description = "Restores a soft-deleted customer")
	public ResponseEntity<ApiResponse<MessageResponse>> restore(@PathVariable Long userId, Authentication authentication) {
		AuthenticatedUser actor = getAuthenticatedUser(authentication);
		return ResponseEntity.ok(ApiResponse.success(customerService.restore(userId, actor)));
	}

	@GetMapping("/{userId}/activity")
	@Operation(summary = "Customer activity timeline", description = "Returns customer admin actions")
	public ResponseEntity<ApiResponse<CustomerActivityResponse>> activity(
		@PathVariable Long userId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size
	) {
		return ResponseEntity.ok(ApiResponse.success(customerService.getActivity(userId, page, size)));
	}

	private AuthenticatedUser getAuthenticatedUser(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHORIZED", "User is not authenticated");
		}
		return user;
	}
}

