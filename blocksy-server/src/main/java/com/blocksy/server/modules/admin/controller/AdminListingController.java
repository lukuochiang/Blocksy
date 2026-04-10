package com.blocksy.server.modules.admin.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.listing.dto.AdminListingHandleLogResponse;
import com.blocksy.server.modules.listing.dto.AdminListingHandleRequest;
import com.blocksy.server.modules.listing.dto.AdminListingBatchHandleRequest;
import com.blocksy.server.modules.listing.dto.AdminListingBatchHandleResponse;
import com.blocksy.server.modules.listing.dto.AdminListingBatchRetryRequest;
import com.blocksy.server.modules.listing.dto.AdminListingResponse;
import com.blocksy.server.modules.listing.dto.ListingCategoryStatResponse;
import com.blocksy.server.modules.listing.service.ListingService;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import com.blocksy.server.security.service.AdminGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/listings")
@Tag(name = "Admin-Listing", description = "后台分类信息管理")
public class AdminListingController {

    private final ListingService listingService;
    private final AdminGuard adminGuard;

    public AdminListingController(ListingService listingService, AdminGuard adminGuard) {
        this.listingService = listingService;
        this.adminGuard = adminGuard;
    }

    @GetMapping
    @Operation(summary = "后台分类信息列表")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<AdminListingResponse>> list(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "communityId", required = false) Long communityId,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(listingService.pageForAdmin(status, communityId, category, keyword, page, pageSize));
    }

    @PostMapping("/{id}/handle")
    @Operation(summary = "后台处理分类信息（通过/驳回/下架/恢复/删除）")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AdminListingResponse> handle(
            @PathVariable("id") Long id,
            @Valid @RequestBody AdminListingHandleRequest request
    ) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(listingService.handleForAdmin(id, admin.userId(), request.action(), request.note()));
    }

    @PostMapping("/batch-handle")
    @Operation(summary = "后台批量处理分类信息")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AdminListingBatchHandleResponse> batchHandle(@Valid @RequestBody AdminListingBatchHandleRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(
                listingService.batchHandleForAdmin(request.listingIds(), admin.userId(), request.action(), request.note())
        );
    }

    @PostMapping("/batch-retry")
    @Operation(summary = "后台重试批量处理分类信息失败项")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AdminListingBatchHandleResponse> batchRetry(@Valid @RequestBody AdminListingBatchRetryRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        return ApiResponse.success(listingService.batchRetryForAdmin(admin.userId(), request));
    }

    @PostMapping("/batch-retry/export")
    @Operation(summary = "重试分类信息失败项并导出结果 CSV")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> batchRetryAndExport(@Valid @RequestBody AdminListingBatchRetryRequest request) {
        AuthenticatedUser admin = adminGuard.requireAdmin();
        AdminListingBatchHandleResponse response = listingService.batchRetryForAdmin(admin.userId(), request);
        String csv = listingService.exportBatchHandleResultCsv(response);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=listing-batch-retry-result.csv")
                .contentType(MediaType.valueOf("text/csv;charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/stats/category")
    @Operation(summary = "后台分类信息分类维度统计")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<ListingCategoryStatResponse>> categoryStats(
            @RequestParam(value = "communityId", required = false) Long communityId,
            @RequestParam(value = "days", required = false) Integer days,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        adminGuard.requireAdmin();
        return ApiResponse.success(listingService.listCategoryStatsForAdmin(communityId, days, startDate, endDate));
    }

    @GetMapping("/{id}/logs")
    @Operation(summary = "后台获取分类信息处理日志")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<AdminListingHandleLogResponse>> logs(@PathVariable("id") Long id) {
        adminGuard.requireAdmin();
        return ApiResponse.success(listingService.listHandleLogsForAdmin(id));
    }
}
