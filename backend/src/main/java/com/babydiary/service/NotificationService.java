package com.babydiary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.babydiary.entity.Notification;
import com.babydiary.entity.User;
import com.babydiary.mapper.NotificationMapper;
import com.babydiary.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    public void create(Long userId, Long actorId, String type, String targetType, Long targetId, String content) {
        if (userId.equals(actorId)) return; // don't notify self
        Notification n = new Notification();
        n.setUserId(userId);
        n.setActorId(actorId);
        n.setType(type);
        n.setTargetType(targetType);
        n.setTargetId(targetId);
        n.setContent(content);
        n.setIsRead(0);
        notificationMapper.insert(n);
    }

    public Map<String, Object> list(Long userId, int page, int size) {
        IPage<Notification> pageObj = notificationMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreatedAt)
        );
        List<Map<String, Object>> vos = new ArrayList<>();
        for (Notification n : pageObj.getRecords()) {
            User actor = userMapper.selectById(n.getActorId());
            Map<String, Object> vo = new HashMap<>();
            vo.put("id", n.getId());
            vo.put("type", n.getType());
            vo.put("targetType", n.getTargetType());
            vo.put("targetId", n.getTargetId());
            vo.put("content", n.getContent());
            vo.put("isRead", n.getIsRead());
            vo.put("createdAt", n.getCreatedAt());
            vo.put("actorName", actor != null ? actor.getUsername() : "未知");
            vo.put("actorAvatar", actor != null ? actor.getAvatar() : null);
            vos.add(vo);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("records", vos);
        result.put("total", pageObj.getTotal());
        result.put("unreadCount", countUnread(userId));
        return result;
    }

    public long countUnread(Long userId) {
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }

    public void markRead(Long id, Long userId) {
        Notification n = notificationMapper.selectById(id);
        if (n != null && n.getUserId().equals(userId)) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
    }

    public void markAllRead(Long userId) {
        List<Notification> list = notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)
        );
        for (Notification n : list) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
    }
}
