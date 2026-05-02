package com.babydiary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.babydiary.common.PageResult;
import com.babydiary.common.Result;
import com.babydiary.entity.*;
import com.babydiary.mapper.*;
import com.babydiary.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final CommentMapper commentMapper;
    private final BookmarkMapper bookmarkMapper;
    private final FollowMapper followMapper;
    private final InteractService interactService;

    public Result<Post> create(Post post, Long userId) {
        post.setUserId(userId);
        postMapper.insert(post);
        return Result.ok(post);
    }

    public Result<PageResult<PostVO>> list(int page, int size, String category, String keyword) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .orderByDesc(Post::getCreatedAt);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Post::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Post::getTitle, keyword).or().like(Post::getContent, keyword));
        }
        IPage<Post> pageObj = postMapper.selectPage(new Page<>(page, size), wrapper);
        List<PostVO> vos = pageObj.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        PageResult<PostVO> result = new PageResult<>();
        result.setTotal(pageObj.getTotal());
        result.setPages(pageObj.getPages());
        result.setCurrent(pageObj.getCurrent());
        result.setRecords(vos);
        return Result.ok(result);
    }

    public Result<PostVO> detail(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) return Result.error("帖子不存在");
        post.setViewCount(post.getViewCount() + 1);
        postMapper.updateById(post);
        return Result.ok(toVO(post));
    }

    public Result<Post> update(Long id, Post updated, Long userId) {
        Post post = postMapper.selectById(id);
        if (post == null) return Result.error("帖子不存在");
        if (!post.getUserId().equals(userId)) return Result.error(403, "无权操作");
        post.setTitle(updated.getTitle());
        post.setContent(updated.getContent());
        post.setCategory(updated.getCategory());
        postMapper.updateById(post);
        return Result.ok(post);
    }

    public Result<String> delete(Long id, Long userId) {
        Post post = postMapper.selectById(id);
        if (post == null) return Result.error("帖子不存在");
        if (!post.getUserId().equals(userId)) return Result.error(403, "无权操作");
        postMapper.deleteById(id);
        return Result.ok("删除成功");
    }

    private PostVO toVO(Post post) {
        User user = userMapper.selectById(post.getUserId());
        return PostVO.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .authorName(user != null ? user.getUsername() : "未知")
                .authorAvatar(user != null ? user.getAvatar() : null)
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .viewCount(post.getViewCount())
                .likeCount((int) interactService.likeCount("post", post.getId()))
                .commentCount((int) interactService.commentCount("post", post.getId()))
                .createdAt(post.getCreatedAt())
                .build();
    }
}

// InteractService is a separate class handling interactions
