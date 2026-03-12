package com.company.digital.customer.service;

import com.company.digital.auth.dto.MessageResponse;
import com.company.digital.auth.entity.CustomerProfile;
import com.company.digital.auth.entity.User;
import com.company.digital.auth.enums.RoleCode;
import com.company.digital.auth.enums.UserStatus;
import com.company.digital.auth.repository.CustomerProfileRepository;
import com.company.digital.auth.repository.UserRepository;
import com.company.digital.auth.security.AuthenticatedUser;
import com.company.digital.common.exception.ApiException;
import com.company.digital.customer.dto.CustomerActivityItemResponse;
import com.company.digital.customer.dto.CustomerActivityResponse;
import com.company.digital.customer.dto.CustomerDetailResponse;
import com.company.digital.customer.dto.CustomerListItemResponse;
import com.company.digital.customer.dto.CustomerListResponse;
import com.company.digital.customer.dto.UpdateCustomerKycRequest;
import com.company.digital.customer.dto.UpdateCustomerProfileRequest;
import com.company.digital.customer.entity.CustomerAdminAction;
import com.company.digital.customer.enums.CustomerAdminActionType;
import com.company.digital.customer.enums.KycStatus;
import com.company.digital.customer.repository.CustomerAdminActionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(noRollbackFor = ApiException.class)
public class CustomerServiceImpl implements CustomerService {

	private final UserRepository userRepository;
	private final CustomerProfileRepository customerProfileRepository;
	private final CustomerAdminActionRepository customerAdminActionRepository;

	public CustomerServiceImpl(
		UserRepository userRepository,
		CustomerProfileRepository customerProfileRepository,
		CustomerAdminActionRepository customerAdminActionRepository
	) {
		this.userRepository = userRepository;
		this.customerProfileRepository = customerProfileRepository;
		this.customerAdminActionRepository = customerAdminActionRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public CustomerListResponse listCustomers(String search, UserStatus userStatus, KycStatus kycStatus, int page, int size, String sort) {
		Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), parseSort(sort));
		String normalizedSearch = normalizeForSearch(search);
		Page<CustomerProfile> profilePage = normalizedSearch == null
			? customerProfileRepository.findForAdminWithoutSearch(userStatus, kycStatus, false, pageable)
			: customerProfileRepository.findForAdminWithSearch(normalizedSearch, userStatus, kycStatus, false, pageable);

		List<CustomerListItemResponse> items = profilePage.getContent()
			.stream()
			.map(profile -> new CustomerListItemResponse(
				profile.getUser().getId(),
				profile.getFullName(),
				profile.getUser().getEmail(),
				profile.getUser().getMobileNumber(),
				profile.getUser().getStatus(),
				profile.getKycStatus()
			))
			.toList();

		return new CustomerListResponse(items, profilePage.getNumber(), profilePage.getSize(), profilePage.getTotalElements(), profilePage.getTotalPages());
	}

	@Override
	@Transactional(readOnly = true)
	public CustomerDetailResponse getCustomer(Long userId) {
		CustomerProfile profile = getCustomerProfile(userId);
		return toDetail(profile);
	}

	@Override
	public CustomerDetailResponse updateProfile(Long userId, UpdateCustomerProfileRequest request, AuthenticatedUser actor) {
		CustomerProfile profile = getCustomerProfile(userId);
		String beforeState = profileSnapshot(profile);

		if (request.fullName() != null) {
			String fullName = normalizeNullable(request.fullName());
			if (fullName == null) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "CUSTOMER_PROFILE_INVALID", "fullName cannot be blank");
			}
			profile.setFullName(fullName);
		}

		if (request.dateOfBirth() != null) {
			profile.setDateOfBirth(request.dateOfBirth());
		}
		if (request.addressLine1() != null) {
			profile.setAddressLine1(normalizeNullable(request.addressLine1()));
		}
		if (request.addressLine2() != null) {
			profile.setAddressLine2(normalizeNullable(request.addressLine2()));
		}
		if (request.city() != null) {
			profile.setCity(normalizeNullable(request.city()));
		}
		if (request.state() != null) {
			profile.setState(normalizeNullable(request.state()));
		}
		if (request.postalCode() != null) {
			profile.setPostalCode(normalizeNullable(request.postalCode()));
		}
		if (request.country() != null) {
			profile.setCountry(normalizeNullable(request.country()));
		}
		if (request.governmentId() != null) {
			String governmentId = normalizeNullable(request.governmentId());
			if (governmentId != null && customerProfileRepository.existsByGovernmentIdIgnoreCaseAndUserIdNot(governmentId, profile.getUser().getId())) {
				throw new ApiException(HttpStatus.CONFLICT, "CUSTOMER_GOVERNMENT_ID_CONFLICT", "governmentId already exists");
			}
			profile.setGovernmentId(governmentId);
		}
		if (request.governmentIdType() != null) {
			profile.setGovernmentIdType(normalizeNullable(request.governmentIdType()));
		}

		profile.setUpdatedBy(actor.userId());
		customerProfileRepository.save(profile);
		logAction(profile.getUser(), actor.userId(), CustomerAdminActionType.PROFILE_UPDATED, null, beforeState, profileSnapshot(profile));

		return toDetail(profile);
	}

	@Override
	public CustomerDetailResponse updateKyc(Long userId, UpdateCustomerKycRequest request, AuthenticatedUser actor) {
		CustomerProfile profile = getCustomerProfile(userId);
		String beforeState = profileSnapshot(profile);

		profile.setKycStatus(request.kycStatus());
		profile.setKycRemarks(normalizeNullable(request.remarks()));
		profile.setKycReviewedBy(actor.userId());
		profile.setKycReviewedAt(LocalDateTime.now());
		profile.setUpdatedBy(actor.userId());
		customerProfileRepository.save(profile);

		logAction(
			profile.getUser(),
			actor.userId(),
			CustomerAdminActionType.KYC_UPDATED,
			request.remarks(),
			beforeState,
			profileSnapshot(profile)
		);

		return toDetail(profile);
	}

	@Override
	public MessageResponse softDelete(Long userId, AuthenticatedUser actor) {
		User customerUser = userRepository.findByIdAndRoleCodeAndIsDeletedFalse(userId, RoleCode.CUSTOMER)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", "Customer not found"));

		String beforeState = userSnapshot(customerUser);
		customerUser.setDeleted(true);
		customerUser.setDeletedBy(actor.userId());
		customerUser.setDeletedAt(LocalDateTime.now());
		userRepository.save(customerUser);
		logAction(customerUser, actor.userId(), CustomerAdminActionType.SOFT_DELETED, null, beforeState, userSnapshot(customerUser));
		return new MessageResponse("Customer soft-deleted successfully");
	}

	@Override
	public MessageResponse restore(Long userId, AuthenticatedUser actor) {
		User customerUser = userRepository.findByIdAndRoleCode(userId, RoleCode.CUSTOMER)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", "Customer not found"));

		if (!customerUser.isDeleted()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "CUSTOMER_NOT_DELETED", "Customer is not deleted");
		}

		String beforeState = userSnapshot(customerUser);
		customerUser.setDeleted(false);
		customerUser.setDeletedBy(null);
		customerUser.setDeletedAt(null);
		userRepository.save(customerUser);
		logAction(customerUser, actor.userId(), CustomerAdminActionType.RESTORED, null, beforeState, userSnapshot(customerUser));
		return new MessageResponse("Customer restored successfully");
	}

	@Override
	@Transactional(readOnly = true)
	public CustomerActivityResponse getActivity(Long userId, int page, int size) {
		userRepository.findByIdAndRoleCode(userId, RoleCode.CUSTOMER)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", "Customer not found"));

		Page<CustomerAdminAction> actionPage = customerAdminActionRepository.findByCustomerUserIdOrderByCreatedAtDesc(
			userId,
			PageRequest.of(Math.max(page, 0), Math.max(size, 1))
		);

		List<CustomerActivityItemResponse> items = actionPage.getContent()
			.stream()
			.map(action -> new CustomerActivityItemResponse(
				action.getId(),
				action.getAdminUser().getId(),
				action.getActionType(),
				action.getReason(),
				action.getBeforeState(),
				action.getAfterState(),
				action.getCreatedAt()
			))
			.toList();

		return new CustomerActivityResponse(items, actionPage.getNumber(), actionPage.getSize(), actionPage.getTotalElements(), actionPage.getTotalPages());
	}

	private CustomerProfile getCustomerProfile(Long userId) {
		CustomerProfile profile = customerProfileRepository.findByUserId(userId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", "Customer not found"));
		if (profile.getUser().isDeleted()) {
			throw new ApiException(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", "Customer not found");
		}
		if (profile.getUser().getRole().getCode() != RoleCode.CUSTOMER) {
			throw new ApiException(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", "Customer not found");
		}
		return profile;
	}

	private void logAction(
		User customerUser,
		Long actorId,
		CustomerAdminActionType actionType,
		String reason,
		String beforeState,
		String afterState
	) {
		User actor = userRepository.findById(actorId)
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_USER_NOT_FOUND", "Authenticated user not found"));

		CustomerAdminAction action = new CustomerAdminAction();
		action.setCustomerUser(customerUser);
		action.setAdminUser(actor);
		action.setActionType(actionType);
		action.setReason(normalizeNullable(reason));
		action.setBeforeState(beforeState);
		action.setAfterState(afterState);
		customerAdminActionRepository.save(action);
	}

	private CustomerDetailResponse toDetail(CustomerProfile profile) {
		User user = profile.getUser();
		return new CustomerDetailResponse(
			user.getId(),
			profile.getFullName(),
			user.getEmail(),
			user.getMobileNumber(),
			user.getStatus(),
			profile.getKycStatus(),
			profile.getDateOfBirth(),
			profile.getAddressLine1(),
			profile.getAddressLine2(),
			profile.getCity(),
			profile.getState(),
			profile.getPostalCode(),
			profile.getCountry(),
			profile.getGovernmentId(),
			profile.getGovernmentIdType(),
			profile.getKycRemarks(),
			user.isEmailVerified(),
			user.isMobileVerified(),
			user.isDeleted(),
			user.getDeletedAt()
		);
	}

	private String normalizeNullable(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private String normalizeForSearch(String value) {
		String normalized = normalizeNullable(value);
		return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
	}

	private Sort parseSort(String sortValue) {
		if (sortValue == null || sortValue.isBlank()) {
			return Sort.by(Sort.Direction.DESC, "createdAt");
		}
		String[] parts = sortValue.split(",");
		String field = parts[0].trim();
		Sort.Direction direction = parts.length > 1 ? Sort.Direction.fromOptionalString(parts[1].trim()).orElse(Sort.Direction.DESC) : Sort.Direction.DESC;
		String mappedField = switch (field) {
			case "fullName" -> "fullName";
			case "kycStatus" -> "kycStatus";
			case "createdAt" -> "createdAt";
			default -> "createdAt";
		};
		return Sort.by(direction, mappedField);
	}

	private String profileSnapshot(CustomerProfile profile) {
		return "fullName="
			+ profile.getFullName()
			+ ",kycStatus="
			+ profile.getKycStatus()
			+ ",governmentId="
			+ profile.getGovernmentId();
	}

	private String userSnapshot(User user) {
		return "status=" + user.getStatus() + ",isDeleted=" + user.isDeleted() + ",deletedAt=" + user.getDeletedAt();
	}
}

