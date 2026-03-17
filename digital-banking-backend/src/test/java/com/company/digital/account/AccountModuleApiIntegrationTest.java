package com.company.digital.account;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.digital.account.entity.Account;
import com.company.digital.account.enums.AccountStatus;
import com.company.digital.account.repository.AccountRepository;
import com.company.digital.account.repository.AccountStatusHistoryRepository;
import com.company.digital.transaction.repository.AccountTransactionRepository;
import com.company.digital.auth.entity.CustomerProfile;
import com.company.digital.auth.entity.Role;
import com.company.digital.auth.entity.User;
import com.company.digital.auth.enums.RoleCode;
import com.company.digital.auth.enums.UserStatus;
import com.company.digital.auth.repository.AdminProfileRepository;
import com.company.digital.auth.repository.AuthTokenRepository;
import com.company.digital.auth.repository.CustomerProfileRepository;
import com.company.digital.auth.repository.LoginActivityLogRepository;
import com.company.digital.auth.repository.RoleRepository;
import com.company.digital.auth.repository.UserRepository;
import com.company.digital.customer.enums.KycStatus;
import com.company.digital.customer.repository.CustomerAdminActionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
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
class AccountModuleApiIntegrationTest {

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
	private AuthTokenRepository authTokenRepository;

	@Autowired
	private LoginActivityLogRepository loginActivityLogRepository;

	@Autowired
	private AdminProfileRepository adminProfileRepository;

	@Autowired
	private CustomerAdminActionRepository customerAdminActionRepository;

	@Autowired
	private AccountStatusHistoryRepository accountStatusHistoryRepository;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private AccountTransactionRepository accountTransactionRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void cleanDatabase() {
		accountTransactionRepository.deleteAll();
		accountStatusHistoryRepository.deleteAll();
		accountRepository.deleteAll();
		customerAdminActionRepository.deleteAll();
		authTokenRepository.deleteAll();
		loginActivityLogRepository.deleteAll();
		customerProfileRepository.deleteAll();
		adminProfileRepository.deleteAll();
		userRepository.deleteAll();
		roleRepository.deleteAll();
	}

	@Test
	void eligibleCustomerCanCreateAndListOwnAccounts() throws Exception {
		createCustomer("acc-user@test.com", "9000000151", "Acc User", KycStatus.APPROVED);
		String token = loginAndGetToken("acc-user@test.com", "Password1");

		String payload = "{" +
			"\"accountType\":\"SAVINGS\"," +
			"\"currencyCode\":\"INR\"}";
		HttpResponse<String> createResponse = request("POST", "/api/accounts", payload, token);
		assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());
		assertThat(objectMapper.readTree(createResponse.body()).path("data").path("status").asText()).isEqualTo("PENDING_APPROVAL");

		HttpResponse<String> listResponse = request("GET", "/api/accounts?page=0&size=10", null, token);
		assertThat(listResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(objectMapper.readTree(listResponse.body()).path("data").path("items").size()).isEqualTo(1);
	}

	@Test
	void customerWithPendingKycCannotCreateAccount() throws Exception {
		createCustomer("pending-kyc@test.com", "9000000152", "Pending User", KycStatus.PENDING);
		String token = loginAndGetToken("pending-kyc@test.com", "Password1");

		String payload = "{" +
			"\"accountType\":\"SAVINGS\"," +
			"\"currencyCode\":\"INR\"}";
		HttpResponse<String> createResponse = request("POST", "/api/accounts", payload, token);
		assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		assertThat(objectMapper.readTree(createResponse.body()).path("error").path("code").asText()).isEqualTo("ACCOUNT_NOT_ELIGIBLE");
	}

	@Test
	void adminCanApprovePendingAccountAndHistoryIsRecorded() throws Exception {
		createCustomer("acc-admin-flow@test.com", "9000000153", "Customer Three", KycStatus.APPROVED);
		createAdmin("acc-admin@test.com", "9000000197", "Password1");

		String customerToken = loginAndGetToken("acc-admin-flow@test.com", "Password1");
		String createPayload = "{" +
			"\"accountType\":\"CURRENT\"," +
			"\"currencyCode\":\"INR\"}";
		HttpResponse<String> createResponse = request("POST", "/api/accounts", createPayload, customerToken);
		Long accountId = objectMapper.readTree(createResponse.body()).path("data").path("accountId").asLong();

		String adminToken = loginAndGetToken("acc-admin@test.com", "Password1");
		String updatePayload = "{" +
			"\"status\":\"ACTIVE\"," +
			"\"reason\":\"Approved by admin\"}";
		HttpResponse<String> updateResponse = request("PATCH", "/api/admin/accounts/" + accountId + "/status", updatePayload, adminToken);
		assertThat(updateResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(objectMapper.readTree(updateResponse.body()).path("data").path("status").asText()).isEqualTo("ACTIVE");

		Account persisted = accountRepository.findById(accountId).orElseThrow();
		assertThat(persisted.getStatus()).isEqualTo(AccountStatus.ACTIVE);
		assertThat(accountStatusHistoryRepository.findByAccountIdOrderByCreatedAtDesc(accountId, org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements())
			.isGreaterThanOrEqualTo(2);
	}

	private String loginAndGetToken(String loginId, String password) throws Exception {
		String payload = "{" +
			"\"loginId\":\"" + loginId + "\"," +
			"\"password\":\"" + password + "\"," +
			"\"deviceId\":\"D1\"}";
		HttpResponse<String> response = request("POST", "/api/auth/login", payload, null);
		return objectMapper.readTree(response.body()).path("data").path("accessToken").asText();
	}

	private User createAdmin(String email, String mobile, String plainPassword) {
		return createUser(email, mobile, plainPassword, RoleCode.ADMIN, true, true, UserStatus.ACTIVE, "Admin User", KycStatus.APPROVED);
	}

	private User createCustomer(String email, String mobile, String fullName, KycStatus kycStatus) {
		return createUser(email, mobile, "Password1", RoleCode.CUSTOMER, true, true, UserStatus.ACTIVE, fullName, kycStatus);
	}

	private User createUser(
		String email,
		String mobile,
		String plainPassword,
		RoleCode roleCode,
		boolean emailVerified,
		boolean mobileVerified,
		UserStatus status,
		String fullName,
		KycStatus kycStatus
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
		User savedUser = userRepository.save(user);

		if (roleCode == RoleCode.CUSTOMER) {
			CustomerProfile profile = new CustomerProfile();
			profile.setUser(savedUser);
			profile.setFullName(fullName);
			profile.setDateOfBirth(LocalDate.of(1995, 1, 1));
			profile.setKycStatus(kycStatus);
			customerProfileRepository.save(profile);
		}

		return savedUser;
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
}

