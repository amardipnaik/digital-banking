package com.company.digital.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "ApiResponse", description = "Standard API response envelope")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
	@Schema(example = "true") boolean success,
	@Schema(example = "2026-03-11T18:00:00Z") Instant timestamp,
	T data,
	ApiError error
) {
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, Instant.now(), data, null);
	}

	public static <T> ApiResponse<T> error(ApiError error) {
		return new ApiResponse<>(false, Instant.now(), null, error);
	}
}
