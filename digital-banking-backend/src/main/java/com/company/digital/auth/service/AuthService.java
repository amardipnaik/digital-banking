package com.company.digital.auth.service;

import com.company.digital.auth.dto.AuthUserSummaryResponse;
import com.company.digital.auth.dto.ForgotPasswordRequest;
import com.company.digital.auth.dto.LoginRequest;
import com.company.digital.auth.dto.LoginResponse;
import com.company.digital.auth.dto.MeResponse;
import com.company.digital.auth.dto.MessageResponse;
import com.company.digital.auth.dto.RegisterCustomerRequest;
import com.company.digital.auth.dto.ResetPasswordRequest;
import com.company.digital.auth.dto.UpdateMeProfileRequest;
import com.company.digital.auth.dto.UpdateUserStatusRequest;
import com.company.digital.auth.dto.VerificationConfirmRequest;
import com.company.digital.auth.dto.VerificationRequest;
import com.company.digital.auth.security.AuthenticatedUser;

public interface AuthService {
	AuthUserSummaryResponse registerCustomer(RegisterCustomerRequest request);
	LoginResponse login(LoginRequest request, String ipAddress, String userAgent);
	MessageResponse requestVerification(VerificationRequest request);
	MessageResponse confirmVerification(VerificationConfirmRequest request);
	MessageResponse forgotPassword(ForgotPasswordRequest request);
	MessageResponse resetPassword(ResetPasswordRequest request);
	MessageResponse logout(AuthenticatedUser authenticatedUser);
	MeResponse me(AuthenticatedUser authenticatedUser);
	MeResponse updateMyProfile(AuthenticatedUser authenticatedUser, UpdateMeProfileRequest request);
	MessageResponse updateUserStatus(Long userId, UpdateUserStatusRequest request, AuthenticatedUser actor);
}
