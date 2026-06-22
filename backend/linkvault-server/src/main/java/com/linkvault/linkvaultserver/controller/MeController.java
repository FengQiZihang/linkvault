package com.linkvault.linkvaultserver.controller;

import com.linkvault.linkvaultserver.common.ApiResponse;
import com.linkvault.linkvaultserver.dto.auth.UpdateUserRequest;
import com.linkvault.linkvaultserver.service.AuthService;
import com.linkvault.linkvaultserver.vo.auth.UserVO;
import com.linkvault.linkvaultserver.vo.auth.UserLibraryStatsVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class MeController {

    private final AuthService authService; // 当前用户相关业务服务

    @GetMapping
    public ApiResponse<UserVO> me(@RequestHeader("Authorization") String authorization) {
        log.info("获取当前用户接口调用");
        UserVO user = authService.getCurrentUser();
        log.info("获取当前用户接口完成，userId={}, phone={}", user.getUserId(), user.getPhone());
        return ApiResponse.success(user);
    }

    @PutMapping
    public ApiResponse<UserVO> updateMe(@Valid @RequestBody UpdateUserRequest request) {
        log.info("更新当前用户资料接口调用，nickname={}", request.getNickname());
        UserVO user = authService.updateUserProfile(request.getNickname(), request.getAvatarUrl());
        log.info("更新当前用户资料接口完成，userId={}, nickname={}", user.getUserId(), user.getNickname());
        return ApiResponse.success(user);
    }

    @GetMapping("/stats")
    public ApiResponse<UserLibraryStatsVO> getStats() {
        log.info("获取当前用户收藏统计接口调用");
        UserLibraryStatsVO stats = authService.getUserLibraryStats();
        log.info("获取当前用户收藏统计接口完成，totalBookmarkCount={}, tagCount={}",
                stats.getTotalBookmarkCount(), stats.getTagCount());
        return ApiResponse.success(stats);
    }
}
