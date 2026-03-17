package com.company.digital.customer;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.digital.transaction.repository.AccountTransactionRepository;
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
class CustomerModuleApiIntegrationTest {

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
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AccountStatusHistoryRepository accountStatusHistoryRepository;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private AccountTransactionRepository accountTransactionRepository;

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
	void adminCanListCustomers() throws Exception {
		createCustomer("cust1@test.com", "9000000101", "Customer One", KycStatus.APPROVED);
		createCustomer("cust2@test.com", "9000000102", "Customer Two", KycStatus.PENDING);
		createAdmin("admin@test.com", "9000000199", "Password1");

		String token = loginAndGetToken("admin@test.com", "Password1");

		HttpResponse<String> response = request("GET", "/api/admin/customers?page=0&size=10", null, token);
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(objectMapper.readTree(response.body()).path("data").path("items").size()).isEqualTo(2);
	}

	@Test
	void adminCanUpdateKycAndSoftDeleteRestoreCustomer() throws Exception {
		User customer = createCustomer("kyc@test.com", "9000000103", "Kyc User", KycStatus.PENDING);
		createAdmin("admin2@test.com", "9000000198", "Password1");
		String token = loginAndGetToken("admin2@test.com", "Password1");

		String kycPayload = "{" +
			"\"kycStatus\":\"APPROVED\"," +
			"\"remarks\":\"Verified docs\"}";
		HttpResponse<String> kycResponse = request("PATCH", "/api/admin/customers/" + customer.getId() + "/kyc", kycPayload, token);
		assertThat(kycResponse.statusCode()).isEqualTo(HttpStatus.OK.value());

		CustomerProfile updatedProfile = customerProfileRepository.findByUserId(customer.getId()).orElseThrow();
		assertThat(updatedProfile.getKycStatus()).isEqualTo(KycStatus.APPROVED);

		HttpResponse<String> deleteResponse = request("DELETE", "/api/admin/customers/" + customer.getId(), null, token);
		assertThat(deleteResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(userRepository.findById(customer.getId()).orElseThrow().isDeleted()).isTrue();

		HttpResponse<String> restoreResponse = request("PATCH", "/api/admin/customers/" + customer.getId() + "/restore", "{}", token);
		assertThat(restoreResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(userRepository.findById(customer.getId()).orElseThrow().isDeleted()).isFalse();
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
			case "DELETE" -> builder.DELETE();
			case "GET" -> builder.GET();
			default -> throw new IllegalArgumentException("Unsupported method: " + method);
		}

		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}
}

