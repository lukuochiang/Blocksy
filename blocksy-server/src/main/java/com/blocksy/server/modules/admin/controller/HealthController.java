package com.blocksy.server.modules.admin.controller;

import com.blocksy.server.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "基础健康检查")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "健康检查")
    public ApiResponse<String> health() {
        return ApiResponse.success("ok");
    }
}
