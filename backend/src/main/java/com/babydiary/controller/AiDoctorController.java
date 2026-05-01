package com.babydiary.controller;

import com.babydiary.common.Result;
import com.babydiary.dto.AiDoctorDTO;
import com.babydiary.entity.User;
import com.babydiary.mapper.UserMapper;
import com.babydiary.service.DeepSeekService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai-doctor")
@RequiredArgsConstructor
public class AiDoctorController {

    private final DeepSeekService deepSeekService;
    private final UserMapper userMapper;

    @PostMapping("/ask")
    public Result<Map<String, Object>> ask(@Valid @RequestBody AiDoctorDTO dto, Authentication auth) {
        String answer = deepSeekService.chat(dto.getQuestion());
        Long userId = (Long) auth.getPrincipal();
        User user = userMapper.selectById(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("question", dto.getQuestion());
        data.put("answer", answer);
        data.put("anonymous", dto.isAnonymous());
        data.put("username", user != null ? user.getUsername() : "未知");
        return Result.ok(data);
    }
}
