package com.babydiary.controller;

import com.babydiary.common.Result;
import com.babydiary.dto.LoginDTO;
import com.babydiary.dto.RegisterDTO;
import com.babydiary.service.UserService;
import com.babydiary.vo.LoginVO;
import com.babydiary.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
}
