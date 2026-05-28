package com.babydiary.controller;

import com.babydiary.common.Result;
import com.babydiary.service.FriendService;
import com.babydiary.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/add/{id}")
    public Result<String> addFriend(@PathVariable Long id, Authentication auth) {
        return friendService.addFriend((Long) auth.getPrincipal(), id);
    }

    @DeleteMapping("/remove/{id}")
    public Result<String> removeFriend(@PathVariable Long id, Authentication auth) {
        return friendService.removeFriend((Long) auth.getPrincipal(), id);
    }

    @GetMapping("/list")
    public Result<List<UserVO>> getFriendList(Authentication auth) {
        return friendService.getFriendList((Long) auth.getPrincipal());
    }

    @GetMapping("/search")
    public Result<List<UserVO>> searchUsers(@RequestParam String keyword, Authentication auth) {
        return friendService.searchUsers(keyword, (Long) auth.getPrincipal());
    }

    @GetMapping("/requests")
    public Result<List<Map<String, Object>>> getFriendRequests(Authentication auth) {
        return friendService.getFriendRequests((Long) auth.getPrincipal());
    }

    @PostMapping("/accept/{requestId}")
    public Result<String> acceptFriend(@PathVariable Long requestId, Authentication auth) {
        return friendService.acceptFriend((Long) auth.getPrincipal(), requestId);
    }

    @DeleteMapping("/reject/{requestId}")
    public Result<String> rejectFriend(@PathVariable Long requestId, Authentication auth) {
        return friendService.rejectFriend((Long) auth.getPrincipal(), requestId);
    }
}
