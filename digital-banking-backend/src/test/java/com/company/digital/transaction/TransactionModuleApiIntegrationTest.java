package com.company.digital.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.digital.account.entity.Account;
import com.company.digital.account.enums.AccountStatus;
import com.company.digital.account.enums.AccountType;
import com.company.digital.account.repository.AccountRepository;
import com.company.digital.account.repository.AccountStatusHistoryRepository;
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
import com.company.digital.transaction.repository.AccountTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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
class TransactionModuleApiIntegrationTest {

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
	private AccountTransactionRepository accountTransactionRepository;

	@Autowired
	private AccountStatusHistoryRepository accountStatusHistoryRepository;

	@Autowired
	private AccountRepository accountRepository;

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
	void depositAndWithdrawalShouldUpdateBalance() throws Exception {
		User customer = createCustomer("txn1@test.com", "9000000311", "Txn One", KycStatus.APPROVED);
		Account account = createActiveAccount(customer, "100000000001", "INR", new BigDecimal("500.00"));
		String token = loginAndGetToken("txn1@test.com", "Password1");

		String depositPayload = "{" +
			"\"accountId\":" + account.getId() + "," +
			"\"amount\":250.00," +
			"\"currencyCode\":\"INR\"," +
			"\"idempotencyKey\":\"dep-1\"}";
		HttpResponse<String> depositResponse = request("POST", "/api/transactions/deposit", depositPayload, token);
		assertThat(depositResponse.statusCode()).isEqualTo(HttpStatus.OK.value());

		String withdrawPayload = "{" +
			"\"accountId\":" + account.getId() + "," +
			"\"amount\":100.00," +
			"\"currencyCode\":\"INR\"," +
			"\"idempotencyKey\":\"wd-1\"}";
		HttpResponse<String> withdrawalResponse = request("POST", "/api/transactions/withdrawal", withdrawPayload, token);
		assertThat(withdrawalResponse.statusCode()).isEqualTo(HttpStatus.OK.value());

		Account persisted = accountRepository.findById(account.getId()).orElseThrow();
		assertThat(persisted.getAvailableBalance()).isEqualByComparingTo("650.00");
		assertThat(accountTransactionRepository.findAll().size()).isEqualTo(2);
	}

	@Test
	void transferShouldMoveFundsAtomically() throws Exception {
		User sourceUser = createCustomer("txn2@test.com", "9000000312", "Txn Two", KycStatus.APPROVED);
		User targetUser = createCustomer("txn3@test.com", "9000000313", "Txn Three", KycStatus.APPROVED);
		Account source = createActiveAccount(sourceUser, "100000000002", "INR", new BigDecimal("800.00"));
		Account target = createActiveAccount(targetUser, "100000000003", "INR", new BigDecimal("200.00"));
		String token = loginAndGetToken("txn2@test.com", "Password1");

		String payload = "{" +
			"\"sourceAccountId\":" + source.getId() + "," +
			"\"targetAccountId\":" + target.getId() + "," +
			"\"amount\":150.00," +
			"\"currencyCode\":\"INR\"," +
			"\"idempotencyKey\":\"trf-1\"}";
		HttpResponse<String> response = request("POST", "/api/transactions/transfer", payload, token);
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

		Account persistedSource = accountRepository.findById(source.getId()).orElseThrow();
		Account persistedTarget = accountRepository.findById(target.getId()).orElseThrow();
		assertThat(persistedSource.getAvailableBalance()).isEqualByComparingTo("650.00");
		assertThat(persistedTarget.getAvailableBalance()).isEqualByComparingTo("350.00");
		assertThat(accountTransactionRepository.findAll().size()).isEqualTo(2);
	}

	@Test
	void adminCanAdjustAndReverse() throws Exception {
		User customer = createCustomer("txn4@test.com", "9000000314", "Txn Four", KycStatus.APPROVED);
		createAdmin("txn-admin@test.com", "9000000399", "Password1");
		Account account = createActiveAccount(customer, "100000000004", "INR", new BigDecimal("100.00"));
		String adminToken = loginAndGetToken("txn-admin@test.com", "Password1");

		String adjustmentPayload = "{" +
			"\"accountId\":" + account.getId() + "," +
			"\"entrySide\":\"CREDIT\"," +
			"\"amount\":50.00," +
			"\"currencyCode\":\"INR\"," +
			"\"reason\":\"Ops correction\"}";
		HttpResponse<String> adjustmentResponse = request("POST", "/api/admin/transactions/adjustment", adjustmentPayload, adminToken);
		assertThat(adjustmentResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
		String transactionRef = objectMapper.readTree(adjustmentResponse.body()).path("data").path("transactionRef").asText();

		String reversalPayload = "{" +
			"\"originalTransactionRef\":\"" + transactionRef + "\"," +
			"\"reason\":\"Rollback adjustment\"}";
		HttpResponse<String> reversalResponse = request("POST", "/api/admin/transactions/reversal", reversalPayload, adminToken);
		assertThat(reversalResponse.statusCode()).isEqualTo(HttpStatus.OK.value());

		Account persisted = accountRepository.findById(account.getId()).orElseThrow();
		assertThat(persisted.getAvailableBalance()).isEqualByComparingTo("100.00");
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

	private Account createActiveAccount(User owner, String number, String currency, BigDecimal balance) {
		Account account = new Account();
		account.setUser(owner);
		account.setAccountNumber(number);
		account.setAccountType(AccountType.SAVINGS);
		account.setCurrencyCode(currency);
		account.setStatus(AccountStatus.ACTIVE);
		account.setOpenedAt(LocalDateTime.now());
		account.setAvailableBalance(balance);
		account.setLedgerBalance(balance);
		account.setCreatedBy(owner.getId());
		account.setUpdatedBy(owner.getId());
		return accountRepository.save(account);
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

