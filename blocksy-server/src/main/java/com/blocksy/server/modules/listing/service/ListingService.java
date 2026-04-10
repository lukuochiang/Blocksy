package com.blocksy.server.modules.listing.service;

import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.modules.listing.dto.ListingRequest;
import com.blocksy.server.modules.listing.dto.ListingResponse;
import com.blocksy.server.modules.listing.dto.AdminListingHandleLogResponse;
import com.blocksy.server.modules.listing.dto.AdminListingBatchHandleResponse;
import com.blocksy.server.modules.listing.dto.AdminListingBatchRetryRequest;
import com.blocksy.server.modules.listing.dto.AdminListingResponse;
import com.blocksy.server.modules.listing.dto.ListingCategoryStatResponse;

import java.time.LocalDate;
import java.util.List;

public interface ListingService {
    List<ListingResponse> list(
            Long communityId,
            String category,
            String keyword,
            Double minPrice,
            Double maxPrice,
            String sortBy,
            String sortOrder
    );

    ListingResponse getById(Long id);

    List<ListingResponse> listRecommendations(Long listingId, Integer limit);

    List<ListingResponse> listMine(Long userId, Long communityId, Integer status);

    ListingResponse getMineById(Long userId, Long id);

    ListingResponse create(Long userId, ListingRequest request);

    PageResponse<AdminListingResponse> pageForAdmin(Integer status, Long communityId, String category, String keyword, Integer page, Integer pageSize);

    AdminListingResponse handleForAdmin(Long listingId, Long operatorUserId, String action, String note);

    AdminListingBatchHandleResponse batchHandleForAdmin(List<Long> listingIds, Long operatorUserId, String action, String note);

    AdminListingBatchHandleResponse batchRetryForAdmin(Long operatorUserId, AdminListingBatchRetryRequest request);

    String exportBatchHandleResultCsv(AdminListingBatchHandleResponse response);

    List<AdminListingHandleLogResponse> listHandleLogsForAdmin(Long listingId);

    List<ListingCategoryStatResponse> listCategoryStatsForAdmin(Long communityId, Integer days, LocalDate startDate, LocalDate endDate);

    List<AdminListingHandleLogResponse> listHandleLogsForMine(Long userId, Long listingId);

    ListingResponse offlineMine(Long userId, Long listingId, String note);

    ListingResponse resubmitMine(Long userId, Long listingId, String note);

    ListingResponse deleteMine(Long userId, Long listingId, String note);
}
