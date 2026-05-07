package com.babydiary.controller;

import com.babydiary.common.PageResult;
import com.babydiary.common.Result;
import com.babydiary.dto.VideoMomentDTO;
import com.babydiary.entity.VideoMoment;
import com.babydiary.service.FileService;
import com.babydiary.service.VideoMomentService;
import com.babydiary.vo.VideoMomentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/moment")
@RequiredArgsConstructor
public class VideoMomentController {

    private final VideoMomentService videoMomentService;
    private final FileService fileService;

    @PostMapping
    public Result<VideoMoment> create(@Valid @RequestBody VideoMomentDTO dto, Authentication auth) {
        return videoMomentService.create(dto, (Long) auth.getPrincipal());
    }

    @GetMapping("/")
    public Result<PageResult<VideoMomentVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long babyId,
            @RequestParam(required = false) String keyword,
            Authentication auth) {
        return videoMomentService.list(page, size, babyId, keyword);
    }

    @GetMapping("/{id}")
    public Result<VideoMomentVO> detail(@PathVariable Long id) {
        return videoMomentService.detail(id);
    }

    @PutMapping("/{id}")
    public Result<VideoMoment> update(@PathVariable Long id, @Valid @RequestBody VideoMomentDTO dto, Authentication auth) {
        return videoMomentService.update(id, dto, (Long) auth.getPrincipal());
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, Authentication auth) {
        return videoMomentService.delete(id, (Long) auth.getPrincipal());
    }

    @PostMapping("/upload-video")
    public Result<Map<String, String>> uploadVideo(@RequestParam("file") MultipartFile file) {
        String url = fileService.upload(file, "video");
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.ok(data);
    }

    @PostMapping("/upload-cover")
    public Result<Map<String, String>> uploadCover(@RequestParam("file") MultipartFile file) {
        String url = fileService.upload(file, "video");
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.ok(data);
    }
}
