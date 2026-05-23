package com.linkvault.linkvaultserver.controller;

import com.linkvault.linkvaultserver.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
public class PingController {

	@GetMapping("/ping")
    public ApiResponse<Map<String, String>> ping() {
        log.info("健康检查接口调用");
		return ApiResponse.success(Map.of("message", "pong"));
    }
}
