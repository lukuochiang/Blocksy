package com.blocksy.server.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.common.api.PageResponse;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.modules.admin.dto.CommunityEngagementResponse;
import com.blocksy.server.modules.admin.dto.ContentCategoryCreateRequest;
import com.blocksy.server.modules.admin.dto.ContentCategoryResponse;
import com.blocksy.server.modules.admin.dto.MediaAssetResponse;
import com.blocksy.server.modules.admin.entity.AdminOperationLogEntity;
import com.blocksy.server.modules.admin.entity.ContentCategoryEntity;
import com.blocksy.server.modules.admin.mapper.AdminOperationLogMapper;
import com.blocksy.server.modules.admin.mapper.ContentCategoryMapper;
import com.blocksy.server.modules.admin.service.AdminContentOpsService;
import com.blocksy.server.modules.post.entity.PostEntity;
import com.blocksy.server.modules.post.entity.PostMediaEntity;
import com.blocksy.server.modules.post.mapper.PostMapper;
import com.blocksy.server.modules.post.mapper.PostMediaMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminContentOpsServiceImpl implements AdminContentOpsService {

    private final JdbcTemplate jdbcTemplate;
    private final ContentCategoryMapper contentCategoryMapper;
    private final PostMediaMapper postMediaMapper;
    private final PostMapper postMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;

    public AdminContentOpsServiceImpl(
            JdbcTemplate jdbcTemplate,
            ContentCategoryMapper contentCategoryMapper,
            PostMediaMapper postMediaMapper,
            PostMapper postMapper,
            AdminOperationLogMapper adminOperationLogMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.contentCategoryMapper = contentCategoryMapper;
        this.postMediaMapper = postMediaMapper;
        this.postMapper = postMapper;
        this.adminOperationLogMapper = adminOperationLogMapper;
    }

    @Override
    public PageResponse<CommunityEngagementResponse> pageCommunityEngagement(String keyword, Integer page, Integer pageSize) {
        int currentPage = resolvePage(page);
        int size = resolvePageSize(pageSize, 100);
        StringBuilder sqlBuilder = new StringBuilder("""
                SELECT c.id AS community_id,
                       c.name AS community_name,
                       COALESCE(uc.member_count, 0) AS member_count,
                       COALESCE(p.post_count, 0) AS post_count,
                       COALESCE(pc.comment_count, 0) AS comment_count
                FROM communities c
                LEFT JOIN (
                    SELECT community_id, COUNT(1) AS member_count
                    FROM user_community
                    WHERE status = 1
                    GROUP BY community_id
                ) uc ON uc.community_id = c.id
                LEFT JOIN (
                    SELECT community_id, COUNT(1) AS post_count
                    FROM posts
                    WHERE status = 1
                    GROUP BY community_id
                ) p ON p.community_id = c.id
                LEFT JOIN (
                    SELECT p.community_id, COUNT(1) AS comment_count
                    FROM post_comments pc
                    JOIN posts p ON p.id = pc.post_id
                    WHERE pc.status = 1
                    GROUP BY p.community_id
                ) pc ON pc.community_id = c.id
                """);
        List<Object> args = new ArrayList<>();
        String kw = keyword == null || keyword.isBlank() ? null : "%" + keyword.trim() + "%";
        if (kw != null) {
            sqlBuilder.append(" WHERE (c.name ILIKE ? OR c.code ILIKE ?)");
            args.add(kw);
            args.add(kw);
        }
        sqlBuilder.append(" ORDER BY (COALESCE(uc.member_count, 0) + COALESCE(p.post_count, 0) * 2 + COALESCE(pc.comment_count, 0)) DESC, c.id ASC");
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        args.add(size);
        args.add((currentPage - 1) * size);
        List<CommunityEngagementResponse> items = jdbcTemplate.query(
                sqlBuilder.toString(),
                (rs, rowNum) -> {
                    long memberCount = rs.getLong("member_count");
                    long postCount = rs.getLong("post_count");
                    long commentCount = rs.getLong("comment_count");
                    long score = memberCount + postCount * 2 + commentCount;
                    return new CommunityEngagementResponse(
                            rs.getLong("community_id"),
                            rs.getString("community_name"),
                            memberCount,
                            postCount,
                            commentCount,
                            score
                    );
                },
                args.toArray()
        );
        Long total;
        if (kw == null) {
            total = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM communities", Long.class);
        } else {
            total = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM communities WHERE (name ILIKE ? OR code ILIKE ?)",
                    Long.class,
                    kw, kw
            );
        }
        return new PageResponse<>(currentPage, size, total == null ? 0L : total, items);
    }

    @Override
    public PageResponse<ContentCategoryResponse> pageCategories(String module, String keyword, Integer page, Integer pageSize) {
        int currentPage = resolvePage(page);
        int size = resolvePageSize(pageSize, 200);
        LambdaQueryWrapper<ContentCategoryEntity> countQuery = new LambdaQueryWrapper<ContentCategoryEntity>()
                .eq(ContentCategoryEntity::getStatus, 1);
        if (module != null && !module.isBlank()) {
            countQuery.eq(ContentCategoryEntity::getModule, module.trim().toUpperCase(Locale.ROOT));
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            countQuery.and(w -> w.like(ContentCategoryEntity::getCode, kw).or().like(ContentCategoryEntity::getName, kw));
        }
        long total = contentCategoryMapper.selectCount(countQuery);
        if (total == 0) {
            return new PageResponse<>(currentPage, size, 0L, Collections.emptyList());
        }
        LambdaQueryWrapper<ContentCategoryEntity> pageQuery = new LambdaQueryWrapper<ContentCategoryEntity>()
                .eq(ContentCategoryEntity::getStatus, 1)
                .orderByAsc(ContentCategoryEntity::getModule)
                .orderByAsc(ContentCategoryEntity::getSortNo)
                .orderByAsc(ContentCategoryEntity::getId)
                .last("LIMIT " + size + " OFFSET " + ((currentPage - 1) * size));
        if (module != null && !module.isBlank()) {
            pageQuery.eq(ContentCategoryEntity::getModule, module.trim().toUpperCase(Locale.ROOT));
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            pageQuery.and(w -> w.like(ContentCategoryEntity::getCode, kw).or().like(ContentCategoryEntity::getName, kw));
        }
        List<ContentCategoryResponse> items = contentCategoryMapper.selectList(pageQuery).stream().map(this::toCategoryResponse).toList();
        return new PageResponse<>(currentPage, size, total, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentCategoryResponse createCategory(Long operatorUserId, ContentCategoryCreateRequest request) {
        String module = request.module().trim().toUpperCase(Locale.ROOT);
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        ContentCategoryEntity exists = contentCategoryMapper.selectOne(
                new LambdaQueryWrapper<ContentCategoryEntity>()
                        .eq(ContentCategoryEntity::getModule, module)
                        .eq(ContentCategoryEntity::getCode, code)
                        .last("LIMIT 1")
        );
        if (exists != null) {
            throw new BusinessException("分类编码已存在");
        }
        LocalDateTime now = LocalDateTime.now();
        ContentCategoryEntity row = new ContentCategoryEntity();
        row.setModule(module);
        row.setCode(code);
        row.setName(request.name().trim());
        row.setSortNo(request.sortNo() == null ? 0 : request.sortNo());
        row.setEnabled(true);
        row.setStatus(1);
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        contentCategoryMapper.insert(row);
        appendLog("CATEGORY", "CREATE", operatorUserId, "CATEGORY", row.getId(), "module=" + module + ",code=" + code);
        return toCategoryResponse(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentCategoryResponse toggleCategory(Long id, Long operatorUserId, Boolean enabled) {
        ContentCategoryEntity row = contentCategoryMapper.selectById(id);
        if (row == null || row.getStatus() == null || row.getStatus() != 1) {
            throw new BusinessException("分类不存在");
        }
        row.setEnabled(Boolean.TRUE.equals(enabled));
        row.setUpdatedAt(LocalDateTime.now());
        contentCategoryMapper.updateById(row);
        appendLog("CATEGORY", "TOGGLE", operatorUserId, "CATEGORY", row.getId(), "enabled=" + row.getEnabled());
        return toCategoryResponse(row);
    }

    @Override
    public PageResponse<MediaAssetResponse> pagePostMedia(Long communityId, Long postId, Integer status, Integer page, Integer pageSize) {
        int currentPage = resolvePage(page);
        int size = resolvePageSize(pageSize, 200);
        LambdaQueryWrapper<PostMediaEntity> countQuery = new LambdaQueryWrapper<>();
        if (status != null) {
            countQuery.eq(PostMediaEntity::getStatus, status);
        }
        if (postId != null) {
            countQuery.eq(PostMediaEntity::getPostId, postId);
        }
        List<Long> postIdsByCommunity = null;
        if (communityId != null) {
            postIdsByCommunity = postMapper.selectList(
                    new LambdaQueryWrapper<PostEntity>()
                            .eq(PostEntity::getCommunityId, communityId)
                            .select(PostEntity::getId)
                            .last("LIMIT 100000")
            ).stream().map(PostEntity::getId).toList();
            if (postIdsByCommunity.isEmpty()) {
                return new PageResponse<>(currentPage, size, 0L, Collections.emptyList());
            }
            countQuery.in(PostMediaEntity::getPostId, postIdsByCommunity);
        }
        long total = postMediaMapper.selectCount(countQuery);
        if (total == 0) {
            return new PageResponse<>(currentPage, size, 0L, Collections.emptyList());
        }

        LambdaQueryWrapper<PostMediaEntity> pageQuery = new LambdaQueryWrapper<PostMediaEntity>()
                .orderByDesc(PostMediaEntity::getCreatedAt)
                .last("LIMIT " + size + " OFFSET " + ((currentPage - 1) * size));
        if (status != null) {
            pageQuery.eq(PostMediaEntity::getStatus, status);
        }
        if (postId != null) {
            pageQuery.eq(PostMediaEntity::getPostId, postId);
        }
        if (communityId != null && postIdsByCommunity != null && !postIdsByCommunity.isEmpty()) {
            pageQuery.in(PostMediaEntity::getPostId, postIdsByCommunity);
        }
        List<PostMediaEntity> mediaRows = postMediaMapper.selectList(pageQuery);
        List<Long> postIds = mediaRows.stream().map(PostMediaEntity::getPostId).distinct().toList();
        Map<Long, PostEntity> postMap = postIds.isEmpty() ? Collections.emptyMap() : postMapper.selectList(
                new LambdaQueryWrapper<PostEntity>().in(PostEntity::getId, postIds)
        ).stream().collect(Collectors.toMap(PostEntity::getId, item -> item));

        List<MediaAssetResponse> items = new ArrayList<>();
        for (PostMediaEntity media : mediaRows) {
            PostEntity post = postMap.get(media.getPostId());
            items.add(new MediaAssetResponse(
                    media.getId(),
                    media.getPostId(),
                    post == null ? null : post.getCommunityId(),
                    post == null ? null : post.getUserId(),
                    media.getObjectKey(),
                    media.getUrl(),
                    media.getSize(),
                    media.getContentType(),
                    media.getStatus(),
                    media.getCreatedAt()
            ));
        }
        return new PageResponse<>(currentPage, size, total, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MediaAssetResponse offlinePostMedia(Long mediaId, Long operatorUserId) {
        PostMediaEntity media = postMediaMapper.selectById(mediaId);
        if (media == null) {
            throw new BusinessException("媒体不存在");
        }
        media.setStatus(0);
        media.setUpdatedAt(LocalDateTime.now());
        postMediaMapper.updateById(media);
        appendLog("MEDIA", "OFFLINE", operatorUserId, "POST_MEDIA", mediaId, media.getObjectKey());

        PostEntity post = postMapper.selectById(media.getPostId());
        return new MediaAssetResponse(
                media.getId(),
                media.getPostId(),
                post == null ? null : post.getCommunityId(),
                post == null ? null : post.getUserId(),
                media.getObjectKey(),
                media.getUrl(),
                media.getSize(),
                media.getContentType(),
                media.getStatus(),
                media.getCreatedAt()
        );
    }

    private ContentCategoryResponse toCategoryResponse(ContentCategoryEntity row) {
        return new ContentCategoryResponse(
                row.getId(),
                row.getModule(),
                row.getCode(),
                row.getName(),
                row.getSortNo(),
                row.getEnabled(),
                row.getUpdatedAt()
        );
    }

    private void appendLog(String module, String action, Long operatorUserId, String targetType, Long targetId, String details) {
        AdminOperationLogEntity log = new AdminOperationLogEntity();
        LocalDateTime now = LocalDateTime.now();
        log.setModule(module);
        log.setAction(action);
        log.setOperatorUserId(operatorUserId);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetails(details);
        log.setStatus(1);
        log.setCreatedAt(now);
        log.setUpdatedAt(now);
        adminOperationLogMapper.insert(log);
    }

    private int resolvePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int resolvePageSize(Integer pageSize, int max) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, max);
    }
}
