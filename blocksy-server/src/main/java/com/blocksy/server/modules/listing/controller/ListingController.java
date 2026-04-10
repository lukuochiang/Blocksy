package com.blocksy.server.modules.listing.controller;

import com.blocksy.server.common.api.ApiResponse;
import com.blocksy.server.modules.listing.dto.ListingRequest;
import com.blocksy.server.modules.listing.dto.ListingResponse;
import com.blocksy.server.modules.listing.dto.AdminListingHandleLogResponse;
import com.blocksy.server.modules.listing.service.ListingService;
import com.blocksy.server.security.SecurityUtils;
import com.blocksy.server.security.jwt.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@Tag(name = "Listing", description = "分类信息接口")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping
    @Operation(summary = "分类信息列表")
    public ApiResponse<List<ListingResponse>> list(
            @RequestParam(value = "communityId", required = false) Long communityId,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "sortBy", required = false, defaultValue = "CREATED_AT") String sortBy,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "DESC") String sortOrder
    ) {
        return ApiResponse.success(listingService.list(communityId, category, keyword, minPrice, maxPrice, sortBy, sortOrder));
    }

    @GetMapping("/mine")
    @Operation(summary = "我的分类信息")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<ListingResponse>> listMine(
            @RequestParam(value = "communityId", required = false) Long communityId,
            @RequestParam(value = "status", required = false) Integer status
    ) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(listingService.listMine(currentUser.userId(), communityId, status));
    }

    @GetMapping("/mine/{id}")
    @Operation(summary = "我的分类信息详情")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<ListingResponse> detailMine(@PathVariable("id") Long id) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(listingService.getMineById(currentUser.userId(), id));
    }

    @GetMapping("/mine/{id}/logs")
    @Operation(summary = "我的分类信息状态日志")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<AdminListingHandleLogResponse>> mineLogs(@PathVariable("id") Long id) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(listingService.listHandleLogsForMine(currentUser.userId(), id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "分类信息详情")
    public ApiResponse<ListingResponse> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(listingService.getById(id));
    }

    @GetMapping("/{id}/recommendations")
    @Operation(summary = "分类信息相关推荐")
    public ApiResponse<List<ListingResponse>> recommendations(
            @PathVariable("id") Long id,
            @RequestParam(value = "limit", required = false, defaultValue = "6") Integer limit
    ) {
        return ApiResponse.success(listingService.listRecommendations(id, limit));
    }

    @PostMapping
    @Operation(summary = "发布分类信息")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<ListingResponse> create(@Valid @RequestBody ListingRequest request) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(listingService.create(currentUser.userId(), request));
    }

    @PostMapping("/mine/{id}/offline")
    @Operation(summary = "我的分类信息下架")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<ListingResponse> offlineMine(
            @PathVariable("id") Long id,
            @RequestParam(value = "note", required = false) String note
    ) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(listingService.offlineMine(currentUser.userId(), id, note));
    }

    @PostMapping("/mine/{id}/resubmit")
    @Operation(summary = "我的分类信息重新提交审核")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<ListingResponse> resubmitMine(
            @PathVariable("id") Long id,
            @RequestParam(value = "note", required = false) String note
    ) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(listingService.resubmitMine(currentUser.userId(), id, note));
    }

    @PostMapping("/mine/{id}/delete")
    @Operation(summary = "我的分类信息删除")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<ListingResponse> deleteMine(
            @PathVariable("id") Long id,
            @RequestParam(value = "note", required = false) String note
    ) {
        AuthenticatedUser currentUser = SecurityUtils.getCurrentUserOrThrow();
        return ApiResponse.success(listingService.deleteMine(currentUser.userId(), id, note));
    }
}
