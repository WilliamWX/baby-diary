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
    private final DiaryMapper diaryMapper;
    private final PostMapper postMapper;
    private final VideoMomentMapper videoMomentMapper;
    private final DiaryImageMapper diaryImageMapper;
    private final NotificationService notificationService;

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

        // notify target owner
        String content = "赞了你的" + getTargetLabel(targetType);
        Long ownerId = getTargetOwnerId(targetType, targetId);
        if (ownerId != null) {
            notificationService.create(ownerId, userId, "like", targetType, targetId, content);
        }
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

        // notify target owner
        String notifyContent = "评论了你的" + getTargetLabel(targetType) + "：" + (content.length() > 30 ? content.substring(0, 30) + "..." : content);
        Long ownerId = getTargetOwnerId(targetType, targetId);
        if (ownerId != null) {
            notificationService.create(ownerId, userId, "comment", targetType, targetId, notifyContent);
        }
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

        // notify
        notificationService.create(followingId, followerId, "follow", null, null, "关注了你");
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

    public Map<String, Boolean> getUserStatus(Long userId, String targetType, Long targetId) {
        boolean liked = likeRecordMapper.selectCount(new LambdaQueryWrapper<LikeRecord>()
                .eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetType, targetType)
                .eq(LikeRecord::getTargetId, targetId)) > 0;
        boolean bookmarked = bookmarkMapper.selectCount(new LambdaQueryWrapper<Bookmark>()
                .eq(Bookmark::getUserId, userId)
                .eq(Bookmark::getTargetType, targetType)
                .eq(Bookmark::getTargetId, targetId)) > 0;
        Map<String, Boolean> status = new HashMap<>();
        status.put("liked", liked);
        status.put("bookmarked", bookmarked);
        return status;
    }

    public boolean isLiked(Long userId, String targetType, Long targetId) {
        if (userId == null) return false;
        return likeRecordMapper.selectCount(new LambdaQueryWrapper<LikeRecord>()
                .eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetType, targetType)
                .eq(LikeRecord::getTargetId, targetId)) > 0;
    }

    public boolean isBookmarked(Long userId, String targetType, Long targetId) {
        if (userId == null) return false;
        return bookmarkMapper.selectCount(new LambdaQueryWrapper<Bookmark>()
                .eq(Bookmark::getUserId, userId)
                .eq(Bookmark::getTargetType, targetType)
                .eq(Bookmark::getTargetId, targetId)) > 0;
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

    private Long getTargetOwnerId(String targetType, Long targetId) {
        if ("diary".equals(targetType)) {
            Diary diary = diaryMapper.selectById(targetId);
            return diary != null ? diary.getUserId() : null;
        }
        if ("post".equals(targetType)) {
            Post post = postMapper.selectById(targetId);
            return post != null ? post.getUserId() : null;
        }
        if ("moment".equals(targetType)) {
            VideoMoment moment = videoMomentMapper.selectById(targetId);
            return moment != null ? moment.getUserId() : null;
        }
        return null;
    }

    public List<Map<String, Object>> getLikedItems(Long userId) {
        List<LikeRecord> likes = likeRecordMapper.selectList(
                new LambdaQueryWrapper<LikeRecord>().eq(LikeRecord::getUserId, userId)
                        .orderByDesc(LikeRecord::getCreatedAt));
        return likes.stream().map(l -> enrichItem(l.getTargetType(), l.getTargetId(), userId)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getBookmarkedItems(Long userId) {
        List<Bookmark> bookmarks = bookmarkMapper.selectList(
                new LambdaQueryWrapper<Bookmark>().eq(Bookmark::getUserId, userId)
                        .orderByDesc(Bookmark::getCreatedAt));
        return bookmarks.stream().map(b -> enrichItem(b.getTargetType(), b.getTargetId(), userId)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Map<String, Object> enrichItem(String targetType, Long targetId, Long userId) {
        Map<String, Object> item = new HashMap<>();
        item.put("type", targetType);
        if ("diary".equals(targetType)) {
            Diary d = diaryMapper.selectById(targetId);
            if (d == null) return null;
            item.put("id", d.getId());
            item.put("content", d.getContent());
            item.put("createdAt", d.getCreatedAt());
            User u = userMapper.selectById(d.getUserId());
            item.put("authorName", u != null ? u.getUsername() : "未知");
            item.put("authorAvatar", u != null ? u.getAvatar() : null);
            item.put("likeCount", (int) likeCount("diary", d.getId()));
            item.put("commentCount", (int) commentCount("diary", d.getId()));
            List<String> images = diaryImageMapper.selectList(
                    new LambdaQueryWrapper<DiaryImage>().eq(DiaryImage::getDiaryId, d.getId()).orderByAsc(DiaryImage::getSort)
            ).stream().map(DiaryImage::getUrl).collect(Collectors.toList());
            item.put("coverUrl", !images.isEmpty() ? images.get(0) : null);
        } else if ("post".equals(targetType)) {
            Post p = postMapper.selectById(targetId);
            if (p == null) return null;
            item.put("id", p.getId());
            item.put("content", p.getTitle());
            item.put("createdAt", p.getCreatedAt());
            User u = userMapper.selectById(p.getUserId());
            item.put("authorName", u != null ? u.getUsername() : "未知");
            item.put("authorAvatar", u != null ? u.getAvatar() : null);
            item.put("likeCount", (int) likeCount("post", p.getId()));
            item.put("commentCount", (int) commentCount("post", p.getId()));
            item.put("coverUrl", null);
        } else if ("moment".equals(targetType)) {
            VideoMoment m = videoMomentMapper.selectById(targetId);
            if (m == null) return null;
            item.put("id", m.getId());
            item.put("content", m.getDescription());
            item.put("createdAt", m.getCreatedAt());
            User u = userMapper.selectById(m.getUserId());
            item.put("authorName", u != null ? u.getUsername() : "未知");
            item.put("authorAvatar", u != null ? u.getAvatar() : null);
            item.put("likeCount", (int) likeCount("moment", m.getId()));
            item.put("commentCount", (int) commentCount("moment", m.getId()));
            item.put("coverUrl", m.getCoverUrl());
        }
        return item;
    }

    private String getTargetLabel(String targetType) {
        if ("diary".equals(targetType)) return "日记";
        if ("post".equals(targetType)) return "帖子";
        if ("moment".equals(targetType)) return "精彩时刻";
        return "内容";
    }
}
