package com.babydiary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.babydiary.common.Result;
import com.babydiary.entity.Friend;
import com.babydiary.entity.User;
import com.babydiary.mapper.FriendMapper;
import com.babydiary.mapper.UserMapper;
import com.babydiary.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendMapper friendMapper;
    private final UserMapper userMapper;

    @Transactional
    public Result<String> addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            return Result.error("不能添加自己为好友");
        }
        if (userMapper.selectById(friendId) == null) {
            return Result.error("用户不存在");
        }
        // check existing in either direction
        if (isFriend(userId, friendId)) {
            return Result.error("已经是好友了");
        }
        // check pending request already sent
        long pendingCount = friendMapper.selectCount(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId)
                .eq(Friend::getStatus, 0));
        if (pendingCount > 0) {
            return Result.error("已发送好友申请，请等待对方同意");
        }
        // check if the other user already sent a request (auto-accept)
        Friend reverseRequest = friendMapper.selectOne(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, friendId)
                .eq(Friend::getFriendId, userId)
                .eq(Friend::getStatus, 0));
        if (reverseRequest != null) {
            reverseRequest.setStatus(1);
            friendMapper.updateById(reverseRequest);
            return Result.ok("已接受对方的好友申请，你们已成为好友");
        }
        Friend friend = new Friend();
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        friend.setStatus(0);
        friendMapper.insert(friend);
        return Result.ok("好友申请已发送");
    }

    @Transactional
    public Result<String> acceptFriend(Long userId, Long requestId) {
        Friend request = friendMapper.selectById(requestId);
        if (request == null) {
            return Result.error("申请不存在");
        }
        if (!request.getFriendId().equals(userId)) {
            return Result.error(403, "无权操作");
        }
        if (request.getStatus() != null && request.getStatus() == 1) {
            return Result.error("已经是好友了");
        }
        request.setStatus(1);
        friendMapper.updateById(request);
        return Result.ok("已接受好友申请");
    }

    @Transactional
    public Result<String> rejectFriend(Long userId, Long requestId) {
        Friend request = friendMapper.selectById(requestId);
        if (request == null) {
            return Result.error("申请不存在");
        }
        if (!request.getFriendId().equals(userId)) {
            return Result.error(403, "无权操作");
        }
        friendMapper.deleteById(requestId);
        return Result.ok("已拒绝好友申请");
    }

    public Result<List<Map<String, Object>>> getFriendRequests(Long userId) {
        List<Friend> requests = friendMapper.selectList(
                new LambdaQueryWrapper<Friend>()
                        .eq(Friend::getFriendId, userId)
                        .eq(Friend::getStatus, 0)
                        .orderByDesc(Friend::getCreatedAt));
        if (requests.isEmpty()) {
            return Result.ok(List.of());
        }
        Set<Long> requesterIds = requests.stream().map(Friend::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userMapper.selectBatchIds(requesterIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        List<Map<String, Object>> result = requests.stream().map(r -> {
            User u = userMap.get(r.getUserId());
            Map<String, Object> m = new HashMap<>();
            m.put("requestId", r.getId());
            m.put("userId", r.getUserId());
            m.put("username", u != null ? u.getUsername() : "未知");
            m.put("avatar", u != null ? u.getAvatar() : null);
            m.put("createdAt", r.getCreatedAt());
            return m;
        }).collect(Collectors.toList());
        return Result.ok(result);
    }

    @Transactional
    public Result<String> removeFriend(Long userId, Long friendId) {
        // remove any record in either direction (pending or accepted)
        Friend friend = friendMapper.selectOne(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId));
        if (friend == null) {
            friend = friendMapper.selectOne(new LambdaQueryWrapper<Friend>()
                    .eq(Friend::getUserId, friendId).eq(Friend::getFriendId, userId));
        }
        if (friend == null) {
            return Result.error("不是好友关系");
        }
        friendMapper.deleteById(friend.getId());
        return Result.ok("删除成功");
    }

    public Set<Long> getFriendIds(Long userId) {
        Set<Long> ids = new HashSet<>();
        List<Friend> list1 = friendMapper.selectList(
                new LambdaQueryWrapper<Friend>().eq(Friend::getUserId, userId).eq(Friend::getStatus, 1));
        List<Friend> list2 = friendMapper.selectList(
                new LambdaQueryWrapper<Friend>().eq(Friend::getFriendId, userId).eq(Friend::getStatus, 1));
        for (Friend f : list1) ids.add(f.getFriendId());
        for (Friend f : list2) ids.add(f.getUserId());
        return ids;
    }

    public boolean isFriend(Long userId, Long otherId) {
        if (userId == null || otherId == null) return false;
        return friendMapper.selectCount(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, userId).eq(Friend::getFriendId, otherId).eq(Friend::getStatus, 1)) > 0
            || friendMapper.selectCount(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getUserId, otherId).eq(Friend::getFriendId, userId).eq(Friend::getStatus, 1)) > 0;
    }

    public Result<List<UserVO>> getFriendList(Long userId) {
        Set<Long> friendIds = getFriendIds(userId);
        if (friendIds.isEmpty()) {
            return Result.ok(List.of());
        }
        List<User> users = userMapper.selectBatchIds(friendIds);
        List<UserVO> vos = users.stream().map(u -> UserVO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .avatar(u.getAvatar())
                .bio(u.getBio())
                .isFriend(true)
                .friendStatus(1)
                .build()).collect(Collectors.toList());
        return Result.ok(vos);
    }

    public Result<List<UserVO>> searchUsers(String keyword, Long currentUserId) {
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>().like(User::getUsername, keyword));
        Set<Long> friendIds = getFriendIds(currentUserId);
        // pending sent requests
        List<Friend> pendingSent = friendMapper.selectList(
                new LambdaQueryWrapper<Friend>()
                        .eq(Friend::getUserId, currentUserId)
                        .eq(Friend::getStatus, 0));
        Set<Long> pendingIds = pendingSent.stream().map(Friend::getFriendId).collect(Collectors.toSet());
        List<UserVO> vos = users.stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .map(u -> {
                    UserVO.UserVOBuilder builder = UserVO.builder()
                            .id(u.getId())
                            .username(u.getUsername())
                            .avatar(u.getAvatar())
                            .bio(u.getBio());
                    if (friendIds.contains(u.getId())) {
                        builder.isFriend(true).friendStatus(1);
                    } else if (pendingIds.contains(u.getId())) {
                        builder.isFriend(false).friendStatus(0);
                    }
                    return builder.build();
                })
                .collect(Collectors.toList());
        return Result.ok(vos);
    }
}
