package com.linkvault.linkvaultserver.controller;

import com.linkvault.linkvaultserver.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PingController {

	@GetMapping("/api/v1/ping")
    public ApiResponse<Map<String, String>> ping() {
		return ApiResponse.success(Map.of("message", "pong"));
    }
}
