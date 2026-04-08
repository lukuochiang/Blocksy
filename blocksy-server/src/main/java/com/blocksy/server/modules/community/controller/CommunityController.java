package com.blocksy.server.modules.community.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.community.dto.CommunityResponse;
import com.blocksy.server.modules.community.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/communities")
@Tag(name = "Community", description = "社区接口")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping
    @Operation(summary = "获取社区列表")
    public ApiResponse<List<CommunityResponse>> list() {
        return ApiResponse.success(communityService.list());
    }
}
