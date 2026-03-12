package com.company.digital.auth.repository;

import com.company.digital.auth.entity.CustomerProfile;
import com.company.digital.auth.enums.UserStatus;
import com.company.digital.customer.enums.KycStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
	Optional<CustomerProfile> findByUserId(Long userId);

	boolean existsByGovernmentIdIgnoreCaseAndUserIdNot(String governmentId, Long userId);

	@Query(
		"""
			select cp
			from CustomerProfile cp
			join cp.user u
			where u.isDeleted = :isDeleted
			and u.role.code = com.company.digital.auth.enums.RoleCode.CUSTOMER
			and (:userStatus is null or u.status = :userStatus)
			and (:kycStatus is null or cp.kycStatus = :kycStatus)
		"""
	)
	Page<CustomerProfile> findForAdminWithoutSearch(
		@Param("userStatus") UserStatus userStatus,
		@Param("kycStatus") KycStatus kycStatus,
		@Param("isDeleted") boolean isDeleted,
		Pageable pageable
	);

	@Query(
		"""
			select cp
			from CustomerProfile cp
			join cp.user u
			where u.isDeleted = :isDeleted
			and u.role.code = com.company.digital.auth.enums.RoleCode.CUSTOMER
			and (
				lower(cp.fullName) like concat('%', :search, '%')
				or lower(u.email) like concat('%', :search, '%')
				or u.mobileNumber like concat('%', :search, '%')
			)
			and (:userStatus is null or u.status = :userStatus)
			and (:kycStatus is null or cp.kycStatus = :kycStatus)
		"""
	)
	Page<CustomerProfile> findForAdminWithSearch(
		@Param("search") String search,
		@Param("userStatus") UserStatus userStatus,
		@Param("kycStatus") KycStatus kycStatus,
		@Param("isDeleted") boolean isDeleted,
		Pageable pageable
	);
}
