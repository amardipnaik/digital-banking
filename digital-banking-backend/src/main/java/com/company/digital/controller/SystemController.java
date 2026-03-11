package com.company.digital.controller;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {

	@Value("${spring.application.name:digital-banking}")
	private String applicationName;

	@Value("${server.port:8080}")
	private String serverPort;

	@GetMapping("/")
	public Map<String, Object> root() {
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("status", "UP");
		response.put("message", "Application is running");
		response.put("application", applicationName);
		response.put("port", serverPort);
		response.put("timestamp", Instant.now().toString());
		response.put("endpoints", new String[] {"/api/health", "/swagger-ui.html", "/actuator/health"});
		return response;
	}

	@GetMapping("/api/health")
	public Map<String, Object> health() {
		long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("status", "UP");
		response.put("application", applicationName);
		response.put("port", serverPort);
		response.put("timestamp", Instant.now().toString());
		response.put("uptimeMs", uptimeMs);
		return response;
	}
}

