package com.blocksy.server.modules.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blocksy.server.common.exception.BusinessException;
import com.blocksy.server.modules.post.dto.AdminPostResponse;
import com.blocksy.server.modules.post.dto.PostMediaItem;
import com.blocksy.server.modules.post.dto.PostRequest;
import com.blocksy.server.modules.post.dto.PostResponse;
import com.blocksy.server.modules.post.entity.PostEntity;
import com.blocksy.server.modules.post.entity.PostMediaEntity;
import com.blocksy.server.modules.post.mapper.PostMapper;
import com.blocksy.server.modules.post.mapper.PostMediaMapper;
import com.blocksy.server.modules.post.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final PostMediaMapper postMediaMapper;

    public PostServiceImpl(PostMapper postMapper, PostMediaMapper postMediaMapper) {
        this.postMapper = postMapper;
        this.postMediaMapper = postMediaMapper;
    }

    @Override
    public List<PostResponse> list(Long communityId) {
        LambdaQueryWrapper<PostEntity> queryWrapper = new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getStatus, 1)
                .orderByDesc(PostEntity::getCreatedAt)
                .last("LIMIT 30");
        if (communityId != null) {
            queryWrapper.eq(PostEntity::getCommunityId, communityId);
        }
        return buildPostResponses(postMapper.selectList(queryWrapper));
    }

    @Override
    public List<PostResponse> listMine(Long userId, Long communityId) {
        LambdaQueryWrapper<PostEntity> queryWrapper = new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getUserId, userId)
                .eq(PostEntity::getStatus, 1)
                .orderByDesc(PostEntity::getCreatedAt)
                .last("LIMIT 30");
        if (communityId != null) {
            queryWrapper.eq(PostEntity::getCommunityId, communityId);
        }
        return buildPostResponses(postMapper.selectList(queryWrapper));
    }

    @Override
    public PostResponse getById(Long id) {
        PostEntity post = postMapper.selectOne(
                new LambdaQueryWrapper<PostEntity>()
                        .eq(PostEntity::getId, id)
                        .eq(PostEntity::getStatus, 1)
                        .last("LIMIT 1")
        );
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        return buildPostResponses(List.of(post)).get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostResponse create(Long userId, PostRequest request) {
        LocalDateTime now = LocalDateTime.now();
        PostEntity post = new PostEntity();
        post.setUserId(userId);
        post.setCommunityId(request.communityId());
        post.setContent(request.content());
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus(1);
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        postMapper.insert(post);

        List<PostMediaItem> mediaItems = request.media() == null ? Collections.emptyList() : request.media();
        List<PostMediaItem> savedMedia = new ArrayList<>();
        for (int i = 0; i < mediaItems.size(); i++) {
            PostMediaItem mediaItem = mediaItems.get(i);
            PostMediaEntity mediaEntity = new PostMediaEntity();
            mediaEntity.setPostId(post.getId());
            mediaEntity.setObjectKey(mediaItem.objectKey());
            mediaEntity.setUrl(mediaItem.url());
            mediaEntity.setSize(mediaItem.size() == null ? 0L : mediaItem.size());
            mediaEntity.setContentType("image/*");
            mediaEntity.setSortNo(i);
            mediaEntity.setStatus(1);
            mediaEntity.setCreatedAt(now);
            mediaEntity.setUpdatedAt(now);
            postMediaMapper.insert(mediaEntity);
            savedMedia.add(new PostMediaItem(mediaEntity.getObjectKey(), mediaEntity.getUrl(), mediaEntity.getSize()));
        }

        return new PostResponse(
                post.getId(),
                post.getCommunityId(),
                post.getContent(),
                post.getUserId(),
                post.getCommentCount(),
                post.getLikeCount(),
                post.getCreatedAt(),
                savedMedia
        );
    }

    @Override
    public List<AdminPostResponse> listForAdmin(Integer status, Long communityId, String keyword) {
        LambdaQueryWrapper<PostEntity> queryWrapper = new LambdaQueryWrapper<PostEntity>()
                .orderByDesc(PostEntity::getCreatedAt)
                .last("LIMIT 200");
        if (status != null) {
            queryWrapper.eq(PostEntity::getStatus, status);
        }
        if (communityId != null) {
            queryWrapper.eq(PostEntity::getCommunityId, communityId);
        }
        if (keyword != null && !keyword.isBlank()) {
            queryWrapper.like(PostEntity::getContent, keyword.trim());
        }
        return postMapper.selectList(queryWrapper).stream()
                .map(this::toAdminResponse)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminPostResponse reviewForAdmin(Long postId, String action) {
        PostEntity post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        post.setStatus("APPROVE".equalsIgnoreCase(action) ? 1 : 0);
        post.setUpdatedAt(LocalDateTime.now());
        postMapper.updateById(post);
        return toAdminResponse(post);
    }

    private List<PostResponse> buildPostResponses(List<PostEntity> posts) {
        if (posts.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> postIds = posts.stream().map(PostEntity::getId).toList();
        List<PostMediaEntity> mediaList = postMediaMapper.selectList(
                new LambdaQueryWrapper<PostMediaEntity>()
                        .in(PostMediaEntity::getPostId, postIds)
                        .eq(PostMediaEntity::getStatus, 1)
                        .orderByAsc(PostMediaEntity::getSortNo)
        );
        Map<Long, List<PostMediaItem>> mediaMap = mediaList.stream().collect(Collectors.groupingBy(
                PostMediaEntity::getPostId,
                Collectors.mapping(
                        media -> new PostMediaItem(media.getObjectKey(), media.getUrl(), media.getSize()),
                        Collectors.toList()
                )
        ));
        return posts.stream().map(post -> new PostResponse(
                post.getId(),
                post.getCommunityId(),
                post.getContent(),
                post.getUserId(),
                post.getCommentCount(),
                post.getLikeCount(),
                post.getCreatedAt(),
                mediaMap.getOrDefault(post.getId(), Collections.emptyList())
        )).toList();
    }

    private AdminPostResponse toAdminResponse(PostEntity post) {
        return new AdminPostResponse(
                post.getId(),
                post.getUserId(),
                post.getCommunityId(),
                post.getContent(),
                post.getStatus(),
                post.getCommentCount(),
                post.getLikeCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
