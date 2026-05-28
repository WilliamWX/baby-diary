package com.babydiary.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.babydiary.common.Result;
import com.babydiary.dto.AiDoctorDTO;
import com.babydiary.entity.AiChatHistory;
import com.babydiary.entity.User;
import com.babydiary.mapper.AiChatHistoryMapper;
import com.babydiary.mapper.UserMapper;
import com.babydiary.service.DeepSeekService;
import com.babydiary.service.FriendService;
import com.babydiary.service.InteractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/ai-doctor")
@RequiredArgsConstructor
public class AiDoctorController {

    private final DeepSeekService deepSeekService;
    private final UserMapper userMapper;
    private final AiChatHistoryMapper aiChatHistoryMapper;
    private final InteractService interactService;
    private final FriendService friendService;

    @PostMapping("/ask")
    public Result<Map<String, Object>> ask(@Valid @RequestBody AiDoctorDTO dto, Authentication auth) {
        String answer = deepSeekService.chat(dto.getQuestion());
        Long userId = (Long) auth.getPrincipal();
        User user = userMapper.selectById(userId);

        // save to history
        AiChatHistory history = new AiChatHistory();
        history.setUserId(userId);
        history.setQuestion(dto.getQuestion());
        history.setAnswer(answer);
        history.setAnonymous(dto.isAnonymous() ? 1 : 0);
        history.setVisibility(dto.getVisibility() != null ? dto.getVisibility() : 1);
        history.setVisibleTo(dto.getVisibleTo());
        aiChatHistoryMapper.insert(history);

        Map<String, Object> data = new HashMap<>();
        data.put("id", history.getId());
        data.put("question", dto.getQuestion());
        data.put("answer", answer);
        data.put("anonymous", dto.isAnonymous());
        data.put("username", user != null ? user.getUsername() : "未知");
        return Result.ok(data);
    }

    @GetMapping("/history")
    public Result<Map<String, Object>> history(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        IPage<AiChatHistory> pageObj = aiChatHistoryMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<AiChatHistory>()
                        .eq(AiChatHistory::getUserId, userId)
                        .orderByDesc(AiChatHistory::getCreatedAt)
        );
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageObj.getRecords());
        result.put("total", pageObj.getTotal());
        return Result.ok(result);
    }

    @GetMapping("/top")
    public Result<Map<String, Object>> top() {
        List<AiChatHistory> all = aiChatHistoryMapper.selectList(
                new LambdaQueryWrapper<AiChatHistory>()
                        .eq(AiChatHistory::getAnonymous, 0)
                        .eq(AiChatHistory::getVisibility, 1)
                        .orderByDesc(AiChatHistory::getCreatedAt)
        );
        AiChatHistory topChat = null;
        long topLikes = -1;
        for (AiChatHistory chat : all) {
            long likes = interactService.likeCount("aichat", chat.getId());
            if (likes > topLikes) {
                topLikes = likes;
                topChat = chat;
            }
        }
        if (topChat == null) return Result.ok(null);
        User u = userMapper.selectById(topChat.getUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("id", topChat.getId());
        data.put("question", topChat.getQuestion());
        data.put("answer", topChat.getAnswer());
        data.put("likeCount", topLikes);
        data.put("username", u != null ? u.getUsername() : "未知");
        return Result.ok(data);
    }
}
