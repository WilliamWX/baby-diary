package com.babydiary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.babydiary.common.Result;
import com.babydiary.entity.*;
import com.babydiary.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InteractService {

    private final LikeRecordMapper likeRecordMapper;
    private final CommentMapper commentMapper;
    private final BookmarkMapper bookmarkMapper;
    private final FollowMapper followMapper;
    private final UserMapper userMapper;

    // 点赞/取消点赞
    public Result<String> toggleLike(Long userId, String targetType, Long targetId) {
        LikeRecord exist = likeRecordMapper.selectOne(new LambdaQueryWrapper<LikeRecord>()
                .eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetType, targetType)
                .eq(LikeRecord::getTargetId, targetId));
        if (exist != null) {
            likeRecordMapper.deleteById(exist.getId());
            return Result.ok("已取消点赞");
        }
        LikeRecord like = new LikeRecord();
        like.setUserId(userId);
        like.setTargetType(targetType);
        like.setTargetId(targetId);
        likeRecordMapper.insert(like);
        return Result.ok("点赞成功");
    }

    // 评论
    public Result<Comment> addComment(Long userId, String targetType, Long targetId, Long parentId, String content) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setTargetType(targetType);
        comment.setTargetId(targetId);
        comment.setParentId(parentId);
        comment.setContent(content);
        commentMapper.insert(comment);
        return Result.ok(comment);
    }

    public Result<List<Map<String, Object>>> getComments(String targetType, Long targetId) {
        List<Comment> list = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getTargetType, targetType)
                .eq(Comment::getTargetId, targetId)
                .orderByAsc(Comment::getCreatedAt));
        List<Map<String, Object>> vos = list.stream().map(c -> {
            User u = userMapper.selectById(c.getUserId());
            Map<String, Object> vo = new HashMap<>();
            vo.put("id", c.getId());
            vo.put("userId", c.getUserId());
            vo.put("username", u != null ? u.getUsername() : "匿名");
            vo.put("avatar", u != null ? u.getAvatar() : null);
            vo.put("content", c.getContent());
            vo.put("parentId", c.getParentId());
            vo.put("createdAt", c.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
        return Result.ok(vos);
    }

    // 收藏/取消
    public Result<String> toggleBookmark(Long userId, String targetType, Long targetId) {
        Bookmark exist = bookmarkMapper.selectOne(new LambdaQueryWrapper<Bookmark>()
                .eq(Bookmark::getUserId, userId)
                .eq(Bookmark::getTargetType, targetType)
                .eq(Bookmark::getTargetId, targetId));
        if (exist != null) {
            bookmarkMapper.deleteById(exist.getId());
            return Result.ok("已取消收藏");
        }
        Bookmark bm = new Bookmark();
        bm.setUserId(userId);
        bm.setTargetType(targetType);
        bm.setTargetId(targetId);
        bookmarkMapper.insert(bm);
        return Result.ok("收藏成功");
    }

    // 关注/取消
    public Result<String> toggleFollow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            return Result.error("不能关注自己");
        }
        Follow exist = followMapper.selectOne(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, followerId)
                .eq(Follow::getFollowingId, followingId));
        if (exist != null) {
            followMapper.deleteById(exist.getId());
            return Result.ok("已取消关注");
        }
        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        followMapper.insert(follow);
        return Result.ok("关注成功");
    }

    public Result<List<Map<String, Object>>> getFollowers(Long userId) {
        List<Follow> follows = followMapper.selectList(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowingId, userId));
        return Result.ok(toUserList(follows, true));
    }

    public Result<List<Map<String, Object>>> getFollowing(Long userId) {
        List<Follow> follows = followMapper.selectList(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, userId));
        return Result.ok(toUserList(follows, false));
    }

    private List<Map<String, Object>> toUserList(List<Follow> follows, boolean isFollowers) {
        return follows.stream().map(f -> {
            Long uid = isFollowers ? f.getFollowerId() : f.getFollowingId();
            User u = userMapper.selectById(uid);
            Map<String, Object> vo = new HashMap<>();
            vo.put("id", u.getId());
            vo.put("username", u.getUsername());
            vo.put("avatar", u.getAvatar());
            return vo;
        }).collect(Collectors.toList());
    }

    public long likeCount(String targetType, Long targetId) {
        return likeRecordMapper.selectCount(new LambdaQueryWrapper<LikeRecord>()
                .eq(LikeRecord::getTargetType, targetType)
                .eq(LikeRecord::getTargetId, targetId));
    }

    public long commentCount(String targetType, Long targetId) {
        return commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getTargetType, targetType)
                .eq(Comment::getTargetId, targetId));
    }
}
