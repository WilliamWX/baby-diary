package com.babydiary.controller;

import com.babydiary.common.PageResult;
import com.babydiary.common.Result;
import com.babydiary.dto.DiaryDTO;
import com.babydiary.entity.Diary;
import com.babydiary.service.DiaryService;
import com.babydiary.service.FileService;
import com.babydiary.vo.DiaryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final FileService fileService;

    @PostMapping
    public Result<Diary> create(@Valid @RequestBody DiaryDTO dto, Authentication auth) {
        return diaryService.create(dto, (Long) auth.getPrincipal());
    }

    @GetMapping("/")
    public Result<PageResult<DiaryVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        return diaryService.list(page, size, (Long) auth.getPrincipal());
    }

    @GetMapping("/{id}")
    public Result<DiaryVO> detail(@PathVariable Long id) {
        return diaryService.detail(id);
    }

    @PutMapping("/{id}")
    public Result<Diary> update(@PathVariable Long id, @Valid @RequestBody DiaryDTO dto, Authentication auth) {
        diaryService.delete(id, (Long) auth.getPrincipal());
        return diaryService.create(dto, (Long) auth.getPrincipal());
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, Authentication auth) {
        return diaryService.delete(id, (Long) auth.getPrincipal());
    }

    @PostMapping("/upload-image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = fileService.upload(file);
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.ok(data);
    }
}
