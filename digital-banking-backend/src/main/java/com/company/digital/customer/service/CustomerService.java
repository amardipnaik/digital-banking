package com.company.digital.customer.service;

import com.company.digital.auth.dto.MessageResponse;
import com.company.digital.auth.enums.UserStatus;
import com.company.digital.auth.security.AuthenticatedUser;
import com.company.digital.customer.dto.CustomerActivityResponse;
import com.company.digital.customer.dto.CustomerDetailResponse;
import com.company.digital.customer.dto.CustomerListResponse;
import com.company.digital.customer.dto.UpdateCustomerKycRequest;
import com.company.digital.customer.dto.UpdateCustomerProfileRequest;
import com.company.digital.customer.enums.KycStatus;

public interface CustomerService {
	CustomerListResponse listCustomers(String search, UserStatus userStatus, KycStatus kycStatus, int page, int size, String sort);

	CustomerDetailResponse getCustomer(Long userId);

	CustomerDetailResponse updateProfile(Long userId, UpdateCustomerProfileRequest request, AuthenticatedUser actor);

	CustomerDetailResponse updateKyc(Long userId, UpdateCustomerKycRequest request, AuthenticatedUser actor);

	MessageResponse softDelete(Long userId, AuthenticatedUser actor);

	MessageResponse restore(Long userId, AuthenticatedUser actor);

	CustomerActivityResponse getActivity(Long userId, int page, int size);
}

