package com.blocksy.server.modules.listing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.modules.listing.dto.AdminListingBatchHandleResponse;
import com.blocksy.server.modules.listing.dto.AdminListingBatchItemResult;
import com.blocksy.server.modules.listing.dto.AdminListingBatchRetryRequest;
import com.blocksy.server.modules.listing.dto.AdminListingHandleLogResponse;
import com.blocksy.server.modules.listing.dto.AdminListingResponse;
import com.blocksy.server.modules.listing.dto.ListingRequest;
import com.blocksy.server.modules.listing.dto.ListingResponse;
import com.blocksy.server.modules.listing.dto.ListingCategoryStatResponse;
import com.blocksy.server.modules.listing.entity.ListingHandleLogEntity;
import com.blocksy.server.modules.listing.entity.ListingEntity;
import com.blocksy.server.modules.listing.mapper.ListingHandleLogMapper;
import com.blocksy.server.modules.listing.mapper.ListingMapper;
import com.blocksy.server.modules.listing.service.ListingService;
import com.blocksy.server.modules.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.time.temporal.ChronoUnit;

@Service
public class ListingServiceImpl implements ListingService {

    private static final Set<String> ALLOWED_CATEGORIES = Set.of("SECOND_HAND", "LOST_FOUND", "HELP_WANTED");
    private final ListingMapper listingMapper;
    private final ListingHandleLogMapper listingHandleLogMapper;
    private final NotificationService notificationService;

    public ListingServiceImpl(
            ListingMapper listingMapper,
            ListingHandleLogMapper listingHandleLogMapper,
            NotificationService notificationService
    ) {
        this.listingMapper = listingMapper;
        this.listingHandleLogMapper = listingHandleLogMapper;
        this.notificationService = notificationService;
    }

    @Override
    public List<ListingResponse> list(
            Long communityId,
            String category,
            String keyword,
            Double minPrice,
            Double maxPrice,
            String sortBy,
            String sortOrder
    ) {
        LambdaQueryWrapper<ListingEntity> query = new LambdaQueryWrapper<ListingEntity>()
                .eq(ListingEntity::getStatus, 1)
                .last("LIMIT 60");
        if (communityId != null) {
            query.eq(ListingEntity::getCommunityId, communityId);
        }
        if (StringUtils.hasText(category) && !"ALL".equalsIgnoreCase(category)) {
            query.eq(ListingEntity::getCategory, category.toUpperCase());
        }
        if (StringUtils.hasText(keyword)) {
            query.and(q -> q.like(ListingEntity::getTitle, keyword.trim()).or().like(ListingEntity::getContent, keyword.trim()));
        }
        if (minPrice != null) {
            query.ge(ListingEntity::getPrice, BigDecimal.valueOf(minPrice));
        }
        if (maxPrice != null) {
            query.le(ListingEntity::getPrice, BigDecimal.valueOf(maxPrice));
        }

        String normalizedSortBy = sortBy == null ? "CREATED_AT" : sortBy.trim().toUpperCase();
        String normalizedSortOrder = sortOrder == null ? "DESC" : sortOrder.trim().toUpperCase();
        boolean asc = "ASC".equals(normalizedSortOrder);
        if ("PRICE".equals(normalizedSortBy)) {
            query.orderBy(true, asc, ListingEntity::getPrice).orderByDesc(ListingEntity::getCreatedAt);
        } else {
            query.orderBy(true, asc, ListingEntity::getCreatedAt);
        }
        return listingMapper.selectList(query).stream().map(this::toResponse).toList();
    }

    @Override
    public ListingResponse getById(Long id) {
        ListingEntity entity = listingMapper.selectOne(
                new LambdaQueryWrapper<ListingEntity>()
                        .eq(ListingEntity::getId, id)
                        .eq(ListingEntity::getStatus, 1)
                        .last("LIMIT 1")
        );
        if (entity == null) {
            throw new BusinessException("分类信息不存在");
        }
        return toResponse(entity);
    }

    @Override
    public List<ListingResponse> listRecommendations(Long listingId, Integer limit) {
        ListingEntity source = listingMapper.selectOne(
                new LambdaQueryWrapper<ListingEntity>()
                        .eq(ListingEntity::getId, listingId)
                        .eq(ListingEntity::getStatus, 1)
                        .last("LIMIT 1")
        );
        if (source == null) {
            throw new BusinessException("分类信息不存在");
        }
        int resolvedLimit = limit == null || limit < 1 ? 6 : Math.min(limit, 20);

        List<ListingEntity> rows = listingMapper.selectList(
                new LambdaQueryWrapper<ListingEntity>()
                        .eq(ListingEntity::getStatus, 1)
                        .eq(source.getCommunityId() != null, ListingEntity::getCommunityId, source.getCommunityId())
                        .eq(StringUtils.hasText(source.getCategory()), ListingEntity::getCategory, source.getCategory())
                        .ne(ListingEntity::getId, listingId)
                        .orderByDesc(ListingEntity::getCreatedAt)
                        .last("LIMIT " + resolvedLimit)
        );
        if (rows.size() < resolvedLimit) {
            List<Long> existedIds = rows.stream().map(ListingEntity::getId).toList();
            List<ListingEntity> fallback = listingMapper.selectList(
                    new LambdaQueryWrapper<ListingEntity>()
                            .eq(ListingEntity::getStatus, 1)
                            .eq(source.getCommunityId() != null, ListingEntity::getCommunityId, source.getCommunityId())
                            .ne(ListingEntity::getId, listingId)
                            .notIn(!existedIds.isEmpty(), ListingEntity::getId, existedIds)
                            .orderByDesc(ListingEntity::getCreatedAt)
                            .last("LIMIT " + (resolvedLimit - rows.size()))
            );
            rows.addAll(fallback);
        }
        return rows.stream().map(this::toResponse).toList();
    }

    @Override
    public List<ListingResponse> listMine(Long userId, Long communityId, Integer status) {
        LambdaQueryWrapper<ListingEntity> query = new LambdaQueryWrapper<ListingEntity>()
                .eq(ListingEntity::getUserId, userId)
                .orderByDesc(ListingEntity::getCreatedAt)
                .last("LIMIT 60");
        if (communityId != null) {
            query.eq(ListingEntity::getCommunityId, communityId);
        }
        if (status != null) {
            query.eq(ListingEntity::getStatus, status);
        }
        return listingMapper.selectList(query).stream().map(this::toResponse).toList();
    }

    @Override
    public ListingResponse getMineById(Long userId, Long id) {
        ListingEntity entity = listingMapper.selectOne(
                new LambdaQueryWrapper<ListingEntity>()
                        .eq(ListingEntity::getId, id)
                        .eq(ListingEntity::getUserId, userId)
                        .last("LIMIT 1")
        );
        if (entity == null) {
            throw new BusinessException("分类信息不存在");
        }
        return toResponse(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ListingResponse create(Long userId, ListingRequest request) {
        String normalizedCategory = request.category() == null ? null : request.category().trim().toUpperCase();
        if (!StringUtils.hasText(normalizedCategory) || !ALLOWED_CATEGORIES.contains(normalizedCategory)) {
            throw new BusinessException("不支持的分类，仅支持 SECOND_HAND/LOST_FOUND/HELP_WANTED");
        }
        LocalDateTime now = LocalDateTime.now();
        ListingEntity entity = new ListingEntity();
        entity.setUserId(userId);
        entity.setCommunityId(request.communityId());
        entity.setCategory(normalizedCategory);
        entity.setTitle(request.title());
        entity.setContent(request.content());
        entity.setPrice(request.price() == null ? null : BigDecimal.valueOf(request.price()));
        entity.setContact(request.contact());
        entity.setCoverObjectKey(request.coverObjectKey());
        entity.setCoverUrl(request.coverUrl());
        entity.setStatus(2);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        listingMapper.insert(entity);
        return toResponse(entity);
    }

    @Override
    public PageResponse<AdminListingResponse> pageForAdmin(
            Integer status,
            Long communityId,
            String category,
            String keyword,
            Integer page,
            Integer pageSize
    ) {
        int currentPage = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        LambdaQueryWrapper<ListingEntity> query = new LambdaQueryWrapper<ListingEntity>();
        if (status != null) {
            query.eq(ListingEntity::getStatus, status);
        }
        if (communityId != null) {
            query.eq(ListingEntity::getCommunityId, communityId);
        }
        if (StringUtils.hasText(category) && !"ALL".equalsIgnoreCase(category)) {
            query.eq(ListingEntity::getCategory, category.trim().toUpperCase());
        }
        if (StringUtils.hasText(keyword)) {
            query.and(q -> q.like(ListingEntity::getTitle, keyword.trim()).or().like(ListingEntity::getContent, keyword.trim()));
        }
        long total = listingMapper.selectCount(query);
        if (total == 0) {
            return new PageResponse<>(currentPage, size, 0L, Collections.emptyList());
        }
        List<AdminListingResponse> rows = listingMapper.selectList(
                query.orderByDesc(ListingEntity::getCreatedAt)
                        .last("LIMIT " + size + " OFFSET " + ((currentPage - 1) * size))
        ).stream().map(this::toAdminResponse).toList();
        return new PageResponse<>(currentPage, size, total, rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminListingResponse handleForAdmin(Long listingId, Long operatorUserId, String action, String note) {
        ListingEntity listing = listingMapper.selectById(listingId);
        if (listing == null) {
            throw new BusinessException("分类信息不存在");
        }
        String normalizedAction = normalizeAction(action);
        int targetStatus = targetStatus(normalizedAction);

        listing.setStatus(targetStatus);
        listing.setUpdatedAt(LocalDateTime.now());
        listingMapper.updateById(listing);

        LocalDateTime now = LocalDateTime.now();
        ListingHandleLogEntity log = new ListingHandleLogEntity();
        log.setListingId(listingId);
        log.setOperatorUserId(operatorUserId);
        log.setAction(normalizedAction);
        log.setNote(note);
        log.setStatus(1);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        listingHandleLogMapper.insert(log);
        maybeNotifyListingOwner(listing, operatorUserId, normalizedAction);
        return toAdminResponse(listing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminListingBatchHandleResponse batchHandleForAdmin(List<Long> listingIds, Long operatorUserId, String action, String note) {
        if (listingIds == null || listingIds.isEmpty()) {
            throw new BusinessException("listingIds 不能为空");
        }
        String normalizedAction = normalizeAction(action);
        int targetStatus = targetStatus(normalizedAction);

        List<AdminListingBatchItemResult> results = new ArrayList<>();
        int success = 0;
        for (Long listingId : listingIds) {
            if (listingId == null) {
                results.add(new AdminListingBatchItemResult(null, false, "listingId 不能为空"));
                continue;
            }
            ListingEntity listing = listingMapper.selectById(listingId);
            if (listing == null) {
                results.add(new AdminListingBatchItemResult(listingId, false, "分类信息不存在"));
                continue;
            }
            listing.setStatus(targetStatus);
            listing.setUpdatedAt(LocalDateTime.now());
            listingMapper.updateById(listing);

            LocalDateTime now = LocalDateTime.now();
            ListingHandleLogEntity log = new ListingHandleLogEntity();
            log.setListingId(listingId);
            log.setOperatorUserId(operatorUserId);
            log.setAction(normalizedAction);
            log.setNote(note);
            log.setStatus(1);
            log.setCreatedAt(now);
            log.setUpdatedAt(now);
            listingHandleLogMapper.insert(log);
            maybeNotifyListingOwner(listing, operatorUserId, normalizedAction);

            success++;
            results.add(new AdminListingBatchItemResult(listingId, true, "ok"));
        }
        return new AdminListingBatchHandleResponse(listingIds.size(), success, listingIds.size() - success, results);
    }

    @Override
    public AdminListingBatchHandleResponse batchRetryForAdmin(Long operatorUserId, AdminListingBatchRetryRequest request) {
        return batchHandleForAdmin(request.failedListingIds(), operatorUserId, request.action(), request.note());
    }

    @Override
    public String exportBatchHandleResultCsv(AdminListingBatchHandleResponse response) {
        StringBuilder csv = new StringBuilder("status,listing_id,message\n");
        if (response.items() == null || response.items().isEmpty()) {
            return csv.toString();
        }
        for (AdminListingBatchItemResult row : response.items()) {
            String status = row.success() ? "SUCCESS" : "FAILED";
            csv.append(status)
                    .append(',')
                    .append(row.listingId() == null ? "" : row.listingId())
                    .append(',')
                    .append(escapeCsv(row.message()))
                    .append('\n');
        }
        return csv.toString();
    }

    @Override
    public List<AdminListingHandleLogResponse> listHandleLogsForAdmin(Long listingId) {
        return listingHandleLogMapper.selectList(
                new LambdaQueryWrapper<ListingHandleLogEntity>()
                        .eq(ListingHandleLogEntity::getListingId, listingId)
                        .eq(ListingHandleLogEntity::getStatus, 1)
                        .orderByDesc(ListingHandleLogEntity::getCreatedAt)
                        .last("LIMIT 100")
        ).stream().map(log -> new AdminListingHandleLogResponse(
                log.getId(),
                log.getListingId(),
                log.getOperatorUserId(),
                log.getAction(),
                log.getNote(),
                log.getCreatedAt()
        )).toList();
    }

    @Override
    public List<ListingCategoryStatResponse> listCategoryStatsForAdmin(Long communityId, Integer days, LocalDate startDate, LocalDate endDate) {
        TimeWindow window = resolveWindow(days, startDate, endDate);
        LambdaQueryWrapper<ListingEntity> query = new LambdaQueryWrapper<>();
        if (communityId != null) {
            query.eq(ListingEntity::getCommunityId, communityId);
        }
        query.ge(ListingEntity::getCreatedAt, window.startAt());
        query.lt(ListingEntity::getCreatedAt, window.endExclusive());
        query.select(
                ListingEntity::getCategory,
                ListingEntity::getStatus
        );

        Map<String, long[]> counter = new LinkedHashMap<>();
        counter.put("SECOND_HAND", new long[4]);
        counter.put("LOST_FOUND", new long[4]);
        counter.put("HELP_WANTED", new long[4]);

        for (ListingEntity entity : listingMapper.selectList(query)) {
            String category = entity.getCategory() == null ? "UNKNOWN" : entity.getCategory().trim().toUpperCase();
            counter.putIfAbsent(category, new long[4]);
            long[] values = counter.get(category);
            values[0]++; // total
            if (entity.getStatus() != null && entity.getStatus() == 2) {
                values[1]++; // pending
            } else if (entity.getStatus() != null && entity.getStatus() == 1) {
                values[2]++; // online
            } else {
                values[3]++; // offline
            }
        }
        return counter.entrySet().stream()
                .map(entry -> new ListingCategoryStatResponse(
                        entry.getKey(),
                        entry.getValue()[0],
                        entry.getValue()[1],
                        entry.getValue()[2],
                        entry.getValue()[3]
                ))
                .toList();
    }

    @Override
    public List<AdminListingHandleLogResponse> listHandleLogsForMine(Long userId, Long listingId) {
        ListingEntity listing = listingMapper.selectById(listingId);
        if (listing == null || !userId.equals(listing.getUserId())) {
            throw new BusinessException("分类信息不存在");
        }
        return listHandleLogsForAdmin(listingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ListingResponse offlineMine(Long userId, Long listingId, String note) {
        ListingEntity listing = requireMineListing(userId, listingId);
        if (listing.getStatus() != null && listing.getStatus() == 0) {
            return toResponse(listing);
        }
        listing.setStatus(0);
        listing.setUpdatedAt(LocalDateTime.now());
        listingMapper.updateById(listing);
        appendUserLog(listingId, userId, "USER_OFFLINE", note == null ? "用户主动下架" : note);
        return toResponse(listing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ListingResponse resubmitMine(Long userId, Long listingId, String note) {
        ListingEntity listing = requireMineListing(userId, listingId);
        listing.setStatus(2);
        listing.setUpdatedAt(LocalDateTime.now());
        listingMapper.updateById(listing);
        appendUserLog(listingId, userId, "USER_RESUBMIT", note == null ? "用户重新提交审核" : note);
        return toResponse(listing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ListingResponse deleteMine(Long userId, Long listingId, String note) {
        ListingEntity listing = requireMineListing(userId, listingId);
        listing.setStatus(0);
        listing.setUpdatedAt(LocalDateTime.now());
        listingMapper.updateById(listing);
        appendUserLog(listingId, userId, "USER_DELETE", note == null ? "用户删除发布" : note);
        return toResponse(listing);
    }

    private ListingResponse toResponse(ListingEntity entity) {
        return new ListingResponse(
                entity.getId(),
                entity.getCommunityId(),
                entity.getUserId(),
                entity.getCategory(),
                entity.getTitle(),
                entity.getContent(),
                entity.getPrice() == null ? null : entity.getPrice().doubleValue(),
                entity.getContact(),
                entity.getCoverObjectKey(),
                entity.getCoverUrl(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }

    private AdminListingResponse toAdminResponse(ListingEntity entity) {
        return new AdminListingResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getCommunityId(),
                entity.getCategory(),
                entity.getTitle(),
                entity.getContent(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String normalizeAction(String action) {
        String normalizedAction = action == null ? "" : action.trim().toUpperCase();
        if (!Set.of("APPROVE", "REJECT", "OFFLINE", "RESTORE", "DELETE").contains(normalizedAction)) {
            throw new BusinessException("不支持的动作，仅支持 APPROVE/REJECT/OFFLINE/RESTORE/DELETE");
        }
        return normalizedAction;
    }

    private int targetStatus(String normalizedAction) {
        if ("APPROVE".equals(normalizedAction) || "RESTORE".equals(normalizedAction)) {
            return 1;
        }
        return 0;
    }

    private void maybeNotifyListingOwner(ListingEntity listing, Long operatorUserId, String action) {
        if (listing.getUserId() == null || listing.getUserId().equals(operatorUserId)) {
            return;
        }
        String title = "分类信息审核结果";
        String content;
        if ("APPROVE".equals(action)) {
            content = "你的分类信息《" + listing.getTitle() + "》已审核通过并上架。";
        } else if ("REJECT".equals(action)) {
            content = "你的分类信息《" + listing.getTitle() + "》未通过审核，请修改后重提。";
        } else if ("OFFLINE".equals(action)) {
            content = "你的分类信息《" + listing.getTitle() + "》已被下架。";
        } else if ("RESTORE".equals(action)) {
            content = "你的分类信息《" + listing.getTitle() + "》已恢复展示。";
        } else {
            content = "你的分类信息《" + listing.getTitle() + "》状态已更新。";
        }
        notificationService.create(
                listing.getUserId(),
                "SYSTEM",
                title,
                content,
                listing.getId(),
                "LISTING"
        );
    }

    private ListingEntity requireMineListing(Long userId, Long listingId) {
        ListingEntity listing = listingMapper.selectById(listingId);
        if (listing == null || !userId.equals(listing.getUserId())) {
            throw new BusinessException("分类信息不存在");
        }
        return listing;
    }

    private void appendUserLog(Long listingId, Long userId, String action, String note) {
        LocalDateTime now = LocalDateTime.now();
        ListingHandleLogEntity log = new ListingHandleLogEntity();
        log.setListingId(listingId);
        log.setOperatorUserId(userId);
        log.setAction(action);
        log.setNote(note);
        log.setStatus(1);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        listingHandleLogMapper.insert(log);
    }

    private TimeWindow resolveWindow(Integer days, LocalDate startDate, LocalDate endDate) {
        if (startDate != null || endDate != null) {
            LocalDate resolvedEnd = endDate == null ? LocalDate.now() : endDate;
            LocalDate resolvedStart = startDate == null ? resolvedEnd.minusDays(6) : startDate;
            if (resolvedStart.isAfter(resolvedEnd)) {
                LocalDate temp = resolvedStart;
                resolvedStart = resolvedEnd;
                resolvedEnd = temp;
            }
            long span = ChronoUnit.DAYS.between(resolvedStart, resolvedEnd) + 1;
            int bounded = (int) Math.max(1, Math.min(span, 90));
            LocalDate boundedStart = resolvedEnd.minusDays(bounded - 1L);
            return new TimeWindow(boundedStart.atStartOfDay(), resolvedEnd.plusDays(1).atStartOfDay());
        }
        int resolvedDays = days == null || days < 1 ? 7 : Math.min(days, 90);
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(resolvedDays - 1L);
        return new TimeWindow(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private record TimeWindow(LocalDateTime startAt, LocalDateTime endExclusive) {
    }
}
