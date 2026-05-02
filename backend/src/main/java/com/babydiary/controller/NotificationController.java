package com.babydiary.controller;

import com.babydiary.common.Result;
import com.babydiary.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(notificationService.list(userId, page, size));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(notificationService.countUnread(userId));
    }

    @PutMapping("/{id}/read")
    public Result<String> markRead(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        notificationService.markRead(id, userId);
        return Result.ok("ok");
    }

    @PutMapping("/read-all")
    public Result<String> markAllRead(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        notificationService.markAllRead(userId);
        return Result.ok("ok");
    }
}
