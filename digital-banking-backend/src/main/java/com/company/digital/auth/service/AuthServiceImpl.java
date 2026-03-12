package com.company.digital.auth.service;

import com.company.digital.auth.dto.AuthUserSummaryResponse;
import com.company.digital.auth.dto.ForgotPasswordRequest;
import com.company.digital.auth.dto.LoginRequest;
import com.company.digital.auth.dto.LoginResponse;
import com.company.digital.auth.dto.MeResponse;
import com.company.digital.auth.dto.MessageResponse;
import com.company.digital.auth.dto.RegisterCustomerRequest;
import com.company.digital.auth.dto.ResetPasswordRequest;
import com.company.digital.auth.dto.UpdateUserStatusRequest;
import com.company.digital.auth.dto.VerificationConfirmRequest;
import com.company.digital.auth.dto.VerificationRequest;
import com.company.digital.auth.entity.AuthToken;
import com.company.digital.auth.entity.CustomerProfile;
import com.company.digital.auth.entity.LoginActivityLog;
import com.company.digital.auth.entity.Role;
import com.company.digital.auth.entity.User;
import com.company.digital.auth.enums.LoginResult;
import com.company.digital.auth.enums.RoleCode;
import com.company.digital.auth.enums.TokenChannel;
import com.company.digital.auth.enums.TokenType;
import com.company.digital.auth.enums.UserStatus;
import com.company.digital.auth.repository.AuthTokenRepository;
import com.company.digital.auth.repository.CustomerProfileRepository;
import com.company.digital.auth.repository.LoginActivityLogRepository;
import com.company.digital.auth.repository.RoleRepository;
import com.company.digital.auth.repository.UserRepository;
import com.company.digital.auth.security.AuthenticatedUser;
import com.company.digital.auth.security.JwtService;
import com.company.digital.auth.util.TokenHashingUtil;
import com.company.digital.common.exception.ApiException;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(noRollbackFor = ApiException.class)
public class AuthServiceImpl implements AuthService {

	private static final short MAX_LOGIN_ATTEMPTS = 3;
	private static final int LOCK_MINUTES = 30;
	private static final int VERIFY_TOKEN_EXPIRY_MINUTES = 10;
	private static final int RESET_TOKEN_EXPIRY_MINUTES = 20;
	private static final String DEMO_OTP = "654321";

	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final CustomerProfileRepository customerProfileRepository;
	private final AuthTokenRepository authTokenRepository;
	private final LoginActivityLogRepository loginActivityLogRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthServiceImpl(
		RoleRepository roleRepository,
		UserRepository userRepository,
		CustomerProfileRepository customerProfileRepository,
		AuthTokenRepository authTokenRepository,
		LoginActivityLogRepository loginActivityLogRepository,
		PasswordEncoder passwordEncoder,
		JwtService jwtService
	) {
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.customerProfileRepository = customerProfileRepository;
		this.authTokenRepository = authTokenRepository;
		this.loginActivityLogRepository = loginActivityLogRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Override
	public AuthUserSummaryResponse registerCustomer(RegisterCustomerRequest request) {
		if (!request.password().equals(request.confirmPassword())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "AUTH_PASSWORD_MISMATCH", "Password and confirmPassword must match");
		}

		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new ApiException(HttpStatus.CONFLICT, "AUTH_USER_ALREADY_EXISTS", "Email is already registered");
		}
		if (userRepository.existsByMobileNumber(request.mobileNumber())) {
			throw new ApiException(HttpStatus.CONFLICT, "AUTH_USER_ALREADY_EXISTS", "Mobile number is already registered");
		}

		Role customerRole = getOrCreateRole(RoleCode.CUSTOMER);

		User user = new User();
		user.setRole(customerRole);
		user.setEmail(email);
		user.setMobileNumber(request.mobileNumber());
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setStatus(UserStatus.PENDING_VERIFICATION);
		User savedUser = userRepository.save(user);

		CustomerProfile profile = new CustomerProfile();
		profile.setUser(savedUser);
		profile.setFullName(request.fullName());
		profile.setDateOfBirth(request.dateOfBirth());
		customerProfileRepository.save(profile);

		return toSummary(savedUser);
	}

	@Override
	public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
		User user = findByLoginId(request.loginId())
			.orElseThrow(() -> {
				logAttempt(null, request.loginId(), null, LoginResult.FAILURE, "INVALID_IDENTIFIER", ipAddress, userAgent, request.deviceId());
				return new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "Invalid login credentials");
			});

		unlockIfLockExpired(user);

		if (user.getStatus() == UserStatus.DISABLED) {
			logAttempt(user, request.loginId(), user.getFailedLoginAttempts(), LoginResult.DISABLED, "ACCOUNT_DISABLED", ipAddress, userAgent, request.deviceId());
			throw new ApiException(HttpStatus.FORBIDDEN, "AUTH_ACCOUNT_DISABLED", "Account is disabled");
		}

		if (user.getStatus() == UserStatus.LOCKED && user.getLockUntil() != null && LocalDateTime.now().isBefore(user.getLockUntil())) {
			logAttempt(user, request.loginId(), user.getFailedLoginAttempts(), LoginResult.BLOCKED, "ACCOUNT_LOCKED", ipAddress, userAgent, request.deviceId());
			throw new ApiException(HttpStatus.LOCKED, "AUTH_ACCOUNT_LOCKED", "Account is locked due to failed login attempts");
		}

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			short nextAttempts = (short) (user.getFailedLoginAttempts() + 1);
			user.setFailedLoginAttempts(nextAttempts);
			user.setLastFailedLoginAt(LocalDateTime.now());

			if (nextAttempts >= MAX_LOGIN_ATTEMPTS) {
				user.setStatus(UserStatus.LOCKED);
				user.setLockUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
				logAttempt(user, request.loginId(), nextAttempts, LoginResult.BLOCKED, "ACCOUNT_LOCKED", ipAddress, userAgent, request.deviceId());
			} else {
				logAttempt(user, request.loginId(), nextAttempts, LoginResult.FAILURE, "INVALID_PASSWORD", ipAddress, userAgent, request.deviceId());
			}

			userRepository.save(user);
			if (nextAttempts >= MAX_LOGIN_ATTEMPTS) {
				throw new ApiException(HttpStatus.LOCKED, "AUTH_MAX_LOGIN_ATTEMPTS_REACHED", "Maximum login attempts reached. Account locked.");
			}
			throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "Invalid login credentials");
		}

		if (user.getStatus() != UserStatus.ACTIVE && user.getStatus() != UserStatus.PENDING_VERIFICATION) {
			logAttempt(user, request.loginId(), user.getFailedLoginAttempts(), LoginResult.FAILURE, "ACCOUNT_NOT_ACTIVE", ipAddress, userAgent, request.deviceId());
			throw new ApiException(HttpStatus.FORBIDDEN, "AUTH_ACCOUNT_NOT_ACTIVE", "Account is not active");
		}

		user.setFailedLoginAttempts((short) 0);
		user.setLastFailedLoginAt(null);
		user.setLockUntil(null);
		user.setLastLoginAt(LocalDateTime.now());
		user.setLastLoginIp(ipAddress);
		userRepository.save(user);
		logAttempt(user, request.loginId(), (short) 1, LoginResult.SUCCESS, null, ipAddress, userAgent, request.deviceId());

		String token = jwtService.generateToken(user);
		return new LoginResponse(token, "Bearer", jwtService.getJwtExpirationSeconds(), toSummary(user));
	}

	@Override
	public MessageResponse requestVerification(VerificationRequest request) {
		User user = findByLoginId(request.loginId())
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "AUTH_USER_NOT_FOUND", "User not found"));

		TokenType tokenType = request.channel() == TokenChannel.EMAIL ? TokenType.EMAIL_VERIFY : TokenType.MOBILE_VERIFY;
		List<AuthToken> activeTokens = authTokenRepository.findAllByUserAndTokenTypeAndChannelAndIsUsedFalse(user, tokenType, request.channel());
		for (AuthToken token : activeTokens) {
			token.setUsed(true);
			token.setConsumedAt(LocalDateTime.now());
		}

		String plainToken = generateOtp();
		AuthToken authToken = new AuthToken();
		authToken.setUser(user);
		authToken.setTokenType(tokenType);
		authToken.setChannel(request.channel());
		authToken.setExpiresAt(normalizeTimestamp(LocalDateTime.now().plusMinutes(VERIFY_TOKEN_EXPIRY_MINUTES)));
		authToken.setTokenHash(buildTokenHash(authToken, plainToken));
		authToken.setAttemptCount((short) 0);
		authToken.setMaxAttempts((short) 3);
		authToken.setUsed(false);
		authTokenRepository.save(authToken);

		return new MessageResponse("Verification token generated successfully");
	}

	@Override
	public MessageResponse confirmVerification(VerificationConfirmRequest request) {
		User user = findByLoginId(request.loginId())
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "AUTH_USER_NOT_FOUND", "User not found"));

		TokenType tokenType = request.channel() == TokenChannel.EMAIL ? TokenType.EMAIL_VERIFY : TokenType.MOBILE_VERIFY;
		AuthToken authToken = authTokenRepository
			.findFirstByUserAndTokenTypeAndChannelAndIsUsedFalseOrderByCreatedAtDesc(user, tokenType, request.channel())
			.orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "AUTH_TOKEN_INVALID", "No active verification token found"));

		validateToken(authToken, request.token());

		authToken.setUsed(true);
		authToken.setConsumedAt(LocalDateTime.now());
		if (request.channel() == TokenChannel.EMAIL) {
			user.setEmailVerified(true);
			user.setEmailVerifiedAt(LocalDateTime.now());
		} else {
			user.setMobileVerified(true);
			user.setMobileVerifiedAt(LocalDateTime.now());
		}

		if (user.getStatus() == UserStatus.PENDING_VERIFICATION && user.isEmailVerified() && user.isMobileVerified()) {
			user.setStatus(UserStatus.ACTIVE);
		}

		userRepository.save(user);
		authTokenRepository.save(authToken);
		return new MessageResponse(request.channel().name() + " verified successfully");
	}

	@Override
	public MessageResponse forgotPassword(ForgotPasswordRequest request) {
		findByLoginId(request.loginId()).ifPresent(user -> {
			List<AuthToken> activeTokens = authTokenRepository.findAllByUserAndTokenTypeAndIsUsedFalse(user, TokenType.PASSWORD_RESET);
			for (AuthToken token : activeTokens) {
				token.setUsed(true);
				token.setConsumedAt(LocalDateTime.now());
			}

			String plainToken = generateOtp();
			AuthToken authToken = new AuthToken();
			authToken.setUser(user);
			authToken.setTokenType(TokenType.PASSWORD_RESET);
			authToken.setChannel(null);
			authToken.setExpiresAt(normalizeTimestamp(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES)));
			authToken.setTokenHash(buildTokenHash(authToken, plainToken));
			authToken.setAttemptCount((short) 0);
			authToken.setMaxAttempts((short) 3);
			authToken.setUsed(false);
			authTokenRepository.save(authToken);
		});

		return new MessageResponse("If account exists, password reset instructions have been generated");
	}

	@Override
	public MessageResponse resetPassword(ResetPasswordRequest request) {
		if (!request.newPassword().equals(request.confirmPassword())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "AUTH_PASSWORD_MISMATCH", "newPassword and confirmPassword must match");
		}

		User user = findByLoginId(request.loginId())
			.orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "AUTH_TOKEN_INVALID", "Invalid password reset request"));

		AuthToken authToken = authTokenRepository
			.findFirstByUserAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(user, TokenType.PASSWORD_RESET)
			.orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "AUTH_TOKEN_INVALID", "No active password reset token found"));

		validateToken(authToken, request.token());

		user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
		user.setFailedLoginAttempts((short) 0);
		user.setLastFailedLoginAt(null);
		user.setLockUntil(null);
		if (user.getStatus() == UserStatus.LOCKED) {
			user.setStatus(resolveActiveStatus(user));
		}

		authToken.setUsed(true);
		authToken.setConsumedAt(LocalDateTime.now());

		userRepository.save(user);
		authTokenRepository.save(authToken);
		return new MessageResponse("Password reset successful");
	}

	@Override
	public MessageResponse logout(AuthenticatedUser authenticatedUser) {
		return new MessageResponse("Logout successful");
	}

	@Override
	@Transactional(readOnly = true)
	public MeResponse me(AuthenticatedUser authenticatedUser) {
		User user = userRepository.findByIdAndIsDeletedFalse(authenticatedUser.userId())
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_USER_NOT_FOUND", "Authenticated user not found"));

		return new MeResponse(
			user.getId(),
			user.getRole().getCode().name(),
			user.getEmail(),
			user.getMobileNumber(),
			user.getStatus().name(),
			user.isEmailVerified(),
			user.isMobileVerified()
		);
	}

	@Override
	public MessageResponse updateUserStatus(Long userId, UpdateUserStatusRequest request, AuthenticatedUser actor) {
		if (request.status() != UserStatus.ACTIVE && request.status() != UserStatus.DISABLED) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "AUTH_INVALID_STATUS_TRANSITION", "Only ACTIVE and DISABLED are allowed in this endpoint");
		}

		User user = userRepository.findByIdAndIsDeletedFalse(userId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "AUTH_USER_NOT_FOUND", "User not found"));

		if (request.status() == UserStatus.DISABLED) {
			user.setStatus(UserStatus.DISABLED);
			user.setDisabledReason(request.reason());
			user.setDisabledBy(actor.userId());
			user.setDisabledAt(LocalDateTime.now());
		} else {
			user.setStatus(resolveActiveStatus(user));
			user.setDisabledReason(null);
			user.setDisabledBy(null);
			user.setDisabledAt(null);
			user.setFailedLoginAttempts((short) 0);
			user.setLastFailedLoginAt(null);
			user.setLockUntil(null);
		}

		userRepository.save(user);
		return new MessageResponse("User status updated successfully");
	}

	private void validateToken(AuthToken authToken, String plainToken) {
		String normalizedToken = normalizeToken(plainToken);
		if (normalizedToken.isBlank()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "AUTH_TOKEN_INVALID", "Invalid token");
		}

		if (authToken.isUsed()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "AUTH_TOKEN_ALREADY_USED", "Token has already been used");
		}

		if (LocalDateTime.now().isAfter(authToken.getExpiresAt())) {
			authToken.setUsed(true);
			authToken.setConsumedAt(LocalDateTime.now());
			authTokenRepository.save(authToken);
			throw new ApiException(HttpStatus.BAD_REQUEST, "AUTH_TOKEN_EXPIRED", "Token has expired");
		}

		if (authToken.getAttemptCount() >= authToken.getMaxAttempts()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "AUTH_MAX_TOKEN_ATTEMPTS_REACHED", "Maximum token attempts reached");
		}

		if (!matchesToken(authToken, normalizedToken)) {
			authToken.setAttemptCount((short) (authToken.getAttemptCount() + 1));
			if (authToken.getAttemptCount() >= authToken.getMaxAttempts()) {
				authToken.setUsed(true);
				authToken.setConsumedAt(LocalDateTime.now());
			}
			authTokenRepository.save(authToken);
			throw new ApiException(HttpStatus.BAD_REQUEST, "AUTH_TOKEN_INVALID", "Invalid token");
		}
	}

	private java.util.Optional<User> findByLoginId(String loginId) {
		String trimmed = loginId == null ? "" : loginId.trim();
		if (trimmed.contains("@")) {
			return userRepository.findByEmailIgnoreCase(normalizeEmail(trimmed));
		}
		return userRepository.findByMobileNumber(trimmed);
	}

	private String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
	}

	private Role getOrCreateRole(RoleCode roleCode) {
		return roleRepository.findByCode(roleCode)
			.orElseGet(() -> {
				Role role = new Role();
				role.setCode(roleCode);
				role.setName(roleCode.name());
				role.setDescription(roleCode.name() + " role");
				role.setActive(true);
				return roleRepository.save(role);
			});
	}

	private AuthUserSummaryResponse toSummary(User user) {
		return new AuthUserSummaryResponse(
			user.getId(),
			user.getRole().getCode().name(),
			user.getStatus().name(),
			user.getEmail(),
			user.getMobileNumber(),
			user.isEmailVerified(),
			user.isMobileVerified()
		);
	}

	private String generateOtp() {
		return DEMO_OTP;
	}

	private String buildTokenHash(AuthToken authToken, String plainToken) {
		LocalDateTime normalizedExpiry = normalizeTimestamp(authToken.getExpiresAt());
		String saltedToken = plainToken
			+ "|"
			+ authToken.getUser().getId()
			+ "|"
			+ authToken.getTokenType().name()
			+ "|"
			+ (authToken.getChannel() == null ? "NONE" : authToken.getChannel().name())
			+ "|"
			+ normalizedExpiry;
		return TokenHashingUtil.hash(saltedToken);
	}

	private boolean matchesToken(AuthToken authToken, String plainToken) {
		String legacyHash = TokenHashingUtil.hash(plainToken);
		if (legacyHash.equals(authToken.getTokenHash())) {
			return true;
		}

		if (buildTokenHash(authToken, plainToken).equals(authToken.getTokenHash())) {
			return true;
		}

		return buildLegacyHashes(authToken, plainToken).contains(authToken.getTokenHash());
	}

	private Set<String> buildLegacyHashes(AuthToken authToken, String plainToken) {
		Set<String> hashes = new LinkedHashSet<>();
		LocalDateTime expiresAt = authToken.getExpiresAt();
		if (expiresAt == null) {
			return hashes;
		}

		hashes.add(buildLegacyHashWithExpiry(authToken, plainToken, expiresAt));
		hashes.add(buildLegacyHashWithExpiry(authToken, plainToken, expiresAt.withNano((expiresAt.getNano() / 1_000) * 1_000)));
		hashes.add(buildLegacyHashWithExpiry(authToken, plainToken, expiresAt.withNano((expiresAt.getNano() / 1_000_000) * 1_000_000)));
		hashes.add(buildLegacyHashWithExpiry(authToken, plainToken, expiresAt.withNano(0)));
		return hashes;
	}

	private String buildLegacyHashWithExpiry(AuthToken authToken, String plainToken, LocalDateTime expiresAt) {
		String legacySaltedToken = plainToken
			+ "|"
			+ authToken.getUser().getId()
			+ "|"
			+ authToken.getTokenType().name()
			+ "|"
			+ expiresAt;
		return TokenHashingUtil.hash(legacySaltedToken);
	}

	private String normalizeToken(String token) {
		return token == null ? "" : token.trim();
	}

	private LocalDateTime normalizeTimestamp(LocalDateTime timestamp) {
		if (timestamp == null) {
			return null;
		}
		return timestamp.withNano((timestamp.getNano() / 1_000) * 1_000);
	}

	private void logAttempt(
		User user,
		String loginIdentifier,
		Short attemptNo,
		LoginResult result,
		String failureReason,
		String ipAddress,
		String userAgent,
		String deviceId
	) {
		LoginActivityLog log = new LoginActivityLog();
		log.setUser(user);
		log.setLoginIdentifier(loginIdentifier);
		log.setAttemptNo(attemptNo);
		log.setResult(result);
		log.setFailureReason(failureReason);
		log.setIpAddress(ipAddress);
		log.setUserAgent(userAgent);
		log.setDeviceId(deviceId);
		loginActivityLogRepository.save(log);
	}

	private void unlockIfLockExpired(User user) {
		if (user.getStatus() == UserStatus.LOCKED && user.getLockUntil() != null && LocalDateTime.now().isAfter(user.getLockUntil())) {
			user.setStatus(resolveActiveStatus(user));
			user.setFailedLoginAttempts((short) 0);
			user.setLastFailedLoginAt(null);
			user.setLockUntil(null);
		}
	}

	private UserStatus resolveActiveStatus(User user) {
		if (user.getRole().getCode() == RoleCode.CUSTOMER && (!user.isEmailVerified() || !user.isMobileVerified())) {
			return UserStatus.PENDING_VERIFICATION;
		}
		return UserStatus.ACTIVE;
	}
}
