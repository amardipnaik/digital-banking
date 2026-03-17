package com.company.digital.balance;

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
import com.company.digital.transaction.entity.AccountTransaction;
import com.company.digital.transaction.enums.EntrySide;
import com.company.digital.transaction.enums.TransactionStatus;
import com.company.digital.transaction.enums.TransactionType;
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
class BalanceModuleApiIntegrationTest {

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
	void customerCanViewOwnBalanceAndMiniStatement() throws Exception {
		User customer = createCustomer("bal1@test.com", "9000000411", "Bal User", KycStatus.APPROVED);
		Account account = createActiveAccount(customer, "200000000001", new BigDecimal("1234.00"));
		createTransaction(account, customer, "REF-BAL-1", TransactionType.DEPOSIT, EntrySide.CREDIT, new BigDecimal("100.00"));

		String token = loginAndGetToken("bal1@test.com", "Password1");
		HttpResponse<String> balanceResponse = request("GET", "/api/balances/accounts/" + account.getId(), null, token);
		assertThat(balanceResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(objectMapper.readTree(balanceResponse.body()).path("data").path("availableBalance").decimalValue())
			.isEqualByComparingTo("1234.00");

		HttpResponse<String> statementResponse = request("GET", "/api/balances/accounts/" + account.getId() + "/mini-statement?limit=10", null, token);
		assertThat(statementResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(objectMapper.readTree(statementResponse.body()).path("data").path("items").size()).isEqualTo(1);
	}

	@Test
	void customerCannotViewOtherUsersBalance() throws Exception {
		User owner = createCustomer("bal2@test.com", "9000000412", "Owner", KycStatus.APPROVED);
		User other = createCustomer("bal3@test.com", "9000000413", "Other", KycStatus.APPROVED);
		Account account = createActiveAccount(owner, "200000000002", new BigDecimal("10.00"));

		String token = loginAndGetToken("bal3@test.com", "Password1");
		HttpResponse<String> response = request("GET", "/api/balances/accounts/" + account.getId(), null, token);
		assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
	}

	private User createCustomer(String email, String mobile, String fullName, KycStatus kycStatus) {
		Role role = roleRepository.findByCode(RoleCode.CUSTOMER).orElseGet(() -> {
			Role newRole = new Role();
			newRole.setCode(RoleCode.CUSTOMER);
			newRole.setName(RoleCode.CUSTOMER.name());
			newRole.setDescription(RoleCode.CUSTOMER.name());
			newRole.setActive(true);
			return roleRepository.save(newRole);
		});

		User user = new User();
		user.setRole(role);
		user.setEmail(email);
		user.setMobileNumber(mobile);
		user.setPasswordHash(passwordEncoder.encode("Password1"));
		user.setEmailVerified(true);
		user.setMobileVerified(true);
		user.setEmailVerifiedAt(LocalDateTime.now());
		user.setMobileVerifiedAt(LocalDateTime.now());
		user.setStatus(UserStatus.ACTIVE);
		User saved = userRepository.save(user);

		CustomerProfile profile = new CustomerProfile();
		profile.setUser(saved);
		profile.setFullName(fullName);
		profile.setDateOfBirth(LocalDate.of(1990, 1, 1));
		profile.setKycStatus(kycStatus);
		customerProfileRepository.save(profile);
		return saved;
	}

	private Account createActiveAccount(User owner, String accountNumber, BigDecimal balance) {
		Account account = new Account();
		account.setUser(owner);
		account.setAccountNumber(accountNumber);
		account.setAccountType(AccountType.SAVINGS);
		account.setCurrencyCode("INR");
		account.setStatus(AccountStatus.ACTIVE);
		account.setOpenedAt(LocalDateTime.now());
		account.setAvailableBalance(balance);
		account.setLedgerBalance(balance);
		account.setCreatedBy(owner.getId());
		account.setUpdatedBy(owner.getId());
		return accountRepository.save(account);
	}

	private void createTransaction(Account account, User actor, String ref, TransactionType type, EntrySide side, BigDecimal amount) {
		AccountTransaction transaction = new AccountTransaction();
		transaction.setAccount(account);
		transaction.setTransactionRef(ref);
		transaction.setTransactionType(type);
		transaction.setEntrySide(side);
		transaction.setAmount(amount);
		transaction.setCurrencyCode("INR");
		transaction.setBalanceBefore(new BigDecimal("1134.00"));
		transaction.setBalanceAfter(new BigDecimal("1234.00"));
		transaction.setStatus(TransactionStatus.POSTED);
		transaction.setCreatedBy(actor);
		accountTransactionRepository.save(transaction);
	}

	private String loginAndGetToken(String loginId, String password) throws Exception {
		String payload = "{" +
			"\"loginId\":\"" + loginId + "\"," +
			"\"password\":\"" + password + "\"," +
			"\"deviceId\":\"D1\"}";
		HttpResponse<String> response = request("POST", "/api/auth/login", payload, null);
		return objectMapper.readTree(response.body()).path("data").path("accessToken").asText();
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
			case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
			case "GET" -> builder.GET();
			default -> throw new IllegalArgumentException("Unsupported method: " + method);
		}
		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}
}

