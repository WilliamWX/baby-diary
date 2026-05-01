package com.babydiary.controller;

import com.babydiary.common.Result;
import com.babydiary.dto.LoginDTO;
import com.babydiary.dto.RegisterDTO;
import com.babydiary.service.FileService;
import com.babydiary.service.UserService;
import com.babydiary.vo.LoginVO;
import com.babydiary.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileService fileService;

    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        return userService.register(dto);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return userService.login(dto);
    }

    @GetMapping("/profile")
    public Result<UserVO> profile(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return userService.getProfile(userId);
    }

    @GetMapping("/profile/{id}")
    public Result<UserVO> userProfile(@PathVariable Long id) {
        return userService.getProfile(id);
    }

    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        String url = fileService.upload(file, "avatar");
        userService.updateAvatar(userId, url);
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.ok(data);
    }

    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody Map<String, String> body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        if (body.containsKey("bio")) {
            userService.updateBio(userId, body.get("bio"));
        }
        return Result.ok("更新成功");
    }
}
