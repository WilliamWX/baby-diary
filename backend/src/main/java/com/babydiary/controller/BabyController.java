package com.babydiary.controller;

import com.babydiary.common.Result;
import com.babydiary.dto.BabyDTO;
import com.babydiary.entity.Baby;
import com.babydiary.service.BabyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/baby")
@RequiredArgsConstructor
public class BabyController {

    private final BabyService babyService;

    @PostMapping
    public Result<Baby> create(@Valid @RequestBody BabyDTO dto, Authentication auth) {
        return babyService.create(dto, (Long) auth.getPrincipal());
    }

    @GetMapping
    public Result<List<Baby>> list(Authentication auth) {
        return babyService.list((Long) auth.getPrincipal());
    }

    @GetMapping("/{id}")
    public Result<Baby> detail(@PathVariable Long id) {
        Baby baby = babyService.getById(id);
        if (baby == null) {
            return Result.error("宝宝不存在");
        }
        return Result.ok(baby);
    }

    @PutMapping("/{id}")
    public Result<Baby> update(@PathVariable Long id, @Valid @RequestBody BabyDTO dto, Authentication auth) {
        return babyService.update(id, dto, (Long) auth.getPrincipal());
    }
}
