package com.company.digital.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.digital.auth.entity.AuthToken;
import com.company.digital.auth.entity.Role;
import com.company.digital.auth.entity.User;
import com.company.digital.auth.enums.RoleCode;
import com.company.digital.auth.enums.TokenChannel;
import com.company.digital.auth.enums.TokenType;
import com.company.digital.auth.enums.UserStatus;
import com.company.digital.auth.repository.AdminProfileRepository;
import com.company.digital.auth.repository.AuthTokenRepository;
import com.company.digital.auth.repository.CustomerProfileRepository;
import com.company.digital.auth.repository.LoginActivityLogRepository;
import com.company.digital.auth.repository.RoleRepository;
import com.company.digital.auth.repository.UserRepository;
import com.company.digital.auth.util.TokenHashingUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthModuleApiIntegrationTest {

	@LocalServerPort
	private int port;

	private final HttpClient httpClient = HttpClient.newHttpClient();

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CustomerProfileRepository customerProfileRepository;

	@Autowired
	private AdminProfileRepository adminProfileRepository;

	@Autowired
	private AuthTokenRepository authTokenRepository;

	@Autowired
	private LoginActivityLogRepository loginActivityLogRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void cleanDatabase() {
		authTokenRepository.deleteAll();
		loginActivityLogRepository.deleteAll();
		customerProfileRepository.deleteAll();
		adminProfileRepository.deleteAll();
		userRepository.deleteAll();
		roleRepository.deleteAll();
	}

	@Test
	void registerCustomer_shouldCreatePendingUser() throws Exception {
		String payload = """
			{
			  "fullName":"Anita Sharma",
			  "email":"anita@example.com",
			  "mobileNumber":"9876543210",
			  "dateOfBirth":"1994-07-21",
			  "password":"Password1",
			  "confirmPassword":"Password1"
			}
		""";

		HttpResponse<String> response = request("POST", "/api/auth/register/customer", payload, null);
		assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
		JsonNode body = objectMapper.readTree(response.body());
		assertThat(body.path("success").asBoolean()).isTrue();
		assertThat(body.path("data").path("status").asText()).isEqualTo("PENDING_VERIFICATION");
		assertThat(userRepository.findByEmailIgnoreCase("anita@example.com")).isPresent();
	}

	@Test
	void login_shouldLockAfterThreeInvalidAttempts() throws Exception {
		User user = createUser("lock@test.com", "9000000001", "Password1", RoleCode.CUSTOMER, true, true, UserStatus.ACTIVE);
		String payload = "{" +
			"\"loginId\":\"lock@test.com\"," +
			"\"password\":\"WrongPass1\"," +
			"\"deviceId\":\"D1\"}";

		assertThat(request("POST", "/api/auth/login", payload, null).statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
		assertThat(request("POST", "/api/auth/login", payload, null).statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
		int thirdAttemptStatus = request("POST", "/api/auth/login", payload, null).statusCode();
		assertThat(thirdAttemptStatus).isIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.LOCKED.value());

		User persisted = userRepository.findById(user.getId()).orElseThrow();
		assertThat(persisted.getStatus()).isEqualTo(UserStatus.LOCKED);
	}

	@Test
	void verificationRequest_shouldCreateToken() throws Exception {
		User user = createUser("verify@test.com", "9000000002", "Password1", RoleCode.CUSTOMER, false, false, UserStatus.PENDING_VERIFICATION);
		String payload = "{" +
			"\"loginId\":\"verify@test.com\"," +
			"\"channel\":\"EMAIL\"}";

		HttpResponse<String> response = request("POST", "/api/auth/verification/request", payload, null);
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

		assertThat(authTokenRepository.findFirstByUserAndTokenTypeAndChannelAndIsUsedFalseOrderByCreatedAtDesc(
			user, TokenType.EMAIL_VERIFY, TokenChannel.EMAIL)).isPresent();
	}

	@Test
	void verificationRequest_shouldAllowRepeatRequestsWithSameDemoOtp() throws Exception {
		User user = createUser("repeat@test.com", "9000000012", "Password1", RoleCode.CUSTOMER, false, false, UserStatus.PENDING_VERIFICATION);
		String payload = "{\"loginId\":\"repeat@test.com\",\"channel\":\"EMAIL\"}";

		HttpResponse<String> first = request("POST", "/api/auth/verification/request", payload, null);
		HttpResponse<String> second = request("POST", "/api/auth/verification/request", payload, null);

		assertThat(first.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(second.statusCode()).isEqualTo(HttpStatus.OK.value());

		assertThat(authTokenRepository.findFirstByUserAndTokenTypeAndChannelAndIsUsedFalseOrderByCreatedAtDesc(
			user, TokenType.EMAIL_VERIFY, TokenChannel.EMAIL)).isPresent();
	}

	@Test
	void forgotPassword_shouldGenerateDemoOtpToken() throws Exception {
		User user = createUser("forgot@test.com", "9000000099", "Password1", RoleCode.CUSTOMER, true, true, UserStatus.ACTIVE);
		String payload = "{\"loginId\":\"forgot@test.com\"}";

		HttpResponse<String> response = request("POST", "/api/auth/password/forgot", payload, null);
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

		AuthToken token = authTokenRepository
			.findFirstByUserAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(user, TokenType.PASSWORD_RESET)
			.orElseThrow();
		assertThat(token.getTokenHash()).isNotBlank();
	}

	@Test
	void verificationConfirm_shouldMarkEmailVerified() throws Exception {
		User user = createUser("confirm@test.com", "9000000003", "Password1", RoleCode.CUSTOMER, false, false, UserStatus.PENDING_VERIFICATION);

		AuthToken token = new AuthToken();
		token.setUser(user);
		token.setTokenType(TokenType.EMAIL_VERIFY);
		token.setChannel(TokenChannel.EMAIL);
		token.setTokenHash(TokenHashingUtil.hash("123456"));
		token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
		token.setUsed(false);
		authTokenRepository.save(token);

		String payload = "{" +
			"\"loginId\":\"confirm@test.com\"," +
			"\"channel\":\"EMAIL\"," +
			"\"token\":\"123456\"}";

		HttpResponse<String> response = request("POST", "/api/auth/verification/confirm", payload, null);
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

		User persisted = userRepository.findById(user.getId()).orElseThrow();
		assertThat(persisted.isEmailVerified()).isTrue();
	}

	@Test
	void resetPassword_shouldUpdatePassword() throws Exception {
		User user = createUser("reset@test.com", "9000000004", "Password1", RoleCode.CUSTOMER, true, true, UserStatus.ACTIVE);

		AuthToken token = new AuthToken();
		token.setUser(user);
		token.setTokenType(TokenType.PASSWORD_RESET);
		token.setTokenHash(TokenHashingUtil.hash("reset-123"));
		token.setExpiresAt(LocalDateTime.now().plusMinutes(20));
		token.setUsed(false);
		authTokenRepository.save(token);

		String payload = "{" +
			"\"loginId\":\"reset@test.com\"," +
			"\"token\":\"reset-123\"," +
			"\"newPassword\":\"Password2\"," +
			"\"confirmPassword\":\"Password2\"}";

		HttpResponse<String> response = request("POST", "/api/auth/password/reset", payload, null);
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

		User persisted = userRepository.findById(user.getId()).orElseThrow();
		assertThat(passwordEncoder.matches("Password2", persisted.getPasswordHash())).isTrue();
	}

	@Test
	void forgotThenResetPassword_shouldSucceedWithDemoOtp() throws Exception {
		createUser("otpreset@test.com", "9000000031", "Password1", RoleCode.CUSTOMER, true, true, UserStatus.ACTIVE);

		HttpResponse<String> forgotResponse = request("POST", "/api/auth/password/forgot", "{\"loginId\":\"otpreset@test.com\"}", null);
		assertThat(forgotResponse.statusCode()).isEqualTo(HttpStatus.OK.value());

		String resetPayload = "{" +
			"\"loginId\":\"otpreset@test.com\"," +
			"\"token\":\"654321\"," +
			"\"newPassword\":\"Password2\"," +
			"\"confirmPassword\":\"Password2\"}";

		HttpResponse<String> resetResponse = request("POST", "/api/auth/password/reset", resetPayload, null);
		assertThat(resetResponse.statusCode()).isEqualTo(HttpStatus.OK.value());

		User persisted = userRepository.findByEmailIgnoreCase("otpreset@test.com").orElseThrow();
		assertThat(passwordEncoder.matches("Password2", persisted.getPasswordHash())).isTrue();
	}

	@Test
	void resetPassword_shouldAcceptTrimmedOtpToken() throws Exception {
		createUser("trim-token@test.com", "9000000032", "Password1", RoleCode.CUSTOMER, true, true, UserStatus.ACTIVE);

		HttpResponse<String> forgotResponse = request("POST", "/api/auth/password/forgot", "{\"loginId\":\"trim-token@test.com\"}", null);
		assertThat(forgotResponse.statusCode()).isEqualTo(HttpStatus.OK.value());

		String resetPayload = "{" +
			"\"loginId\":\"trim-token@test.com\"," +
			"\"token\":\" 654321 \"," +
			"\"newPassword\":\"Password2\"," +
			"\"confirmPassword\":\"Password2\"}";

		HttpResponse<String> resetResponse = request("POST", "/api/auth/password/reset", resetPayload, null);
		assertThat(resetResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
	}

	@Test
	void loginThenMe_shouldReturnCurrentUser() throws Exception {
		createUser("me@test.com", "9000000005", "Password1", RoleCode.CUSTOMER, true, true, UserStatus.ACTIVE);

		String loginPayload = "{" +
			"\"loginId\":\"me@test.com\"," +
			"\"password\":\"Password1\"," +
			"\"deviceId\":\"D2\"}";

		HttpResponse<String> loginResponse = request("POST", "/api/auth/login", loginPayload, null);
		assertThat(loginResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
		String token = objectMapper.readTree(loginResponse.body()).path("data").path("accessToken").asText();

		HttpResponse<String> meResponse = request("GET", "/api/auth/me", null, token);
		assertThat(meResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(objectMapper.readTree(meResponse.body()).path("data").path("email").asText()).isEqualTo("me@test.com");
	}

	@Test
	void login_shouldAllowPendingVerificationCustomer() throws Exception {
		createUser("pending@test.com", "9000000008", "Password1", RoleCode.CUSTOMER, false, false, UserStatus.PENDING_VERIFICATION);

		String loginPayload = "{" +
			"\"loginId\":\"pending@test.com\"," +
			"\"password\":\"Password1\"," +
			"\"deviceId\":\"PENDING-1\"}";

		HttpResponse<String> response = request("POST", "/api/auth/login", loginPayload, null);
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
		JsonNode body = objectMapper.readTree(response.body());
		assertThat(body.path("data").path("user").path("status").asText()).isEqualTo("PENDING_VERIFICATION");
		assertThat(body.path("data").path("user").path("emailVerified").asBoolean()).isFalse();
		assertThat(body.path("data").path("user").path("mobileVerified").asBoolean()).isFalse();
	}

	@Test
	void adminCanDisableUser() throws Exception {
		createUser("target@test.com", "9000000006", "Password1", RoleCode.CUSTOMER, true, true, UserStatus.ACTIVE);
		createUser("admin@test.com", "9000000007", "Password1", RoleCode.ADMIN, true, true, UserStatus.ACTIVE);

		HttpResponse<String> login = request("POST", "/api/auth/login", "{" +
			"\"loginId\":\"admin@test.com\"," +
			"\"password\":\"Password1\"," +
			"\"deviceId\":\"ADMIN\"}", null);
		String token = objectMapper.readTree(login.body()).path("data").path("accessToken").asText();

		Long targetId = userRepository.findByEmailIgnoreCase("target@test.com").orElseThrow().getId();
		HttpResponse<String> response = request("PATCH", "/api/admin/auth/users/" + targetId + "/status",
			"{\"status\":\"DISABLED\",\"reason\":\"Security check\"}", token);

		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
		User target = userRepository.findById(targetId).orElseThrow();
		assertThat(target.getStatus()).isEqualTo(UserStatus.DISABLED);
	}

	private HttpResponse<String> request(String method, String path, String jsonBody, String bearerToken) throws Exception {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
			.uri(URI.create("http://localhost:" + port + path))
			.header("Accept", "application/json");

		if (bearerToken != null && !bearerToken.isBlank()) {
			builder.header("Authorization", "Bearer " + bearerToken);
		}
		if (jsonBody != null) {
			builder.header("Content-Type", "application/json");
		}

		switch (method) {
			case "POST" -> builder.POST(jsonBody == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(jsonBody));
			case "PATCH" -> builder.method("PATCH", jsonBody == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(jsonBody));
			case "GET" -> builder.GET();
			default -> throw new IllegalArgumentException("Unsupported method: " + method);
		}

		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	private User createUser(
		String email,
		String mobile,
		String plainPassword,
		RoleCode roleCode,
		boolean emailVerified,
		boolean mobileVerified,
		UserStatus status
	) {
		Role role = roleRepository.findByCode(roleCode).orElseGet(() -> {
			Role newRole = new Role();
			newRole.setCode(roleCode);
			newRole.setName(roleCode.name());
			newRole.setDescription(roleCode.name());
			newRole.setActive(true);
			return roleRepository.save(newRole);
		});

		User user = new User();
		user.setRole(role);
		user.setEmail(email);
		user.setMobileNumber(mobile);
		user.setPasswordHash(passwordEncoder.encode(plainPassword));
		user.setEmailVerified(emailVerified);
		user.setMobileVerified(mobileVerified);
		if (emailVerified) {
			user.setEmailVerifiedAt(LocalDateTime.now());
		}
		if (mobileVerified) {
			user.setMobileVerifiedAt(LocalDateTime.now());
		}
		user.setStatus(status);
		return userRepository.save(user);
	}
}
