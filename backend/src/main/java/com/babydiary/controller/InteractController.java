package com.babydiary.controller;

import com.babydiary.common.Result;
import com.babydiary.dto.CommentDTO;
import com.babydiary.dto.LikeDTO;
import com.babydiary.service.InteractService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/interact")
@RequiredArgsConstructor
public class InteractController {

    private final InteractService interactService;

    @PostMapping("/like")
    public Result<String> like(@RequestBody LikeDTO dto, Authentication auth) {
        return interactService.toggleLike((Long) auth.getPrincipal(), dto.getTargetType(), dto.getTargetId());
    }

    @PostMapping("/comment")
    public Result<?> comment(@RequestBody CommentDTO dto, Authentication auth) {
        return interactService.addComment((Long) auth.getPrincipal(),
                dto.getTargetType(), dto.getTargetId(), dto.getParentId(), dto.getContent());
    }

    @GetMapping("/comment/{type}/{id}")
    public Result<List<Map<String, Object>>> comments(@PathVariable String type, @PathVariable Long id) {
        return interactService.getComments(type, id);
    }

    @PostMapping("/bookmark")
    public Result<String> bookmark(@RequestBody LikeDTO dto, Authentication auth) {
        return interactService.toggleBookmark((Long) auth.getPrincipal(), dto.getTargetType(), dto.getTargetId());
    }

    @PostMapping("/follow/{userId}")
    public Result<String> follow(@PathVariable Long userId, Authentication auth) {
        return interactService.toggleFollow((Long) auth.getPrincipal(), userId);
    }

    @GetMapping("/followers")
    public Result<List<Map<String, Object>>> followers(Authentication auth) {
        return interactService.getFollowers((Long) auth.getPrincipal());
    }

    @GetMapping("/following")
    public Result<List<Map<String, Object>>> following(Authentication auth) {
        return interactService.getFollowing((Long) auth.getPrincipal());
    }
}
