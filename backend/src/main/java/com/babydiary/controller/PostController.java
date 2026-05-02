package com.babydiary.controller;

import com.babydiary.common.PageResult;
import com.babydiary.common.Result;
import com.babydiary.entity.Post;
import com.babydiary.service.PostService;
import com.babydiary.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public Result<Post> create(@RequestBody Post post, Authentication auth) {
        return postService.create(post, (Long) auth.getPrincipal());
    }

    @GetMapping("/")
    public Result<PageResult<PostVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return postService.list(page, size, category, keyword);
    }

    @GetMapping("/{id}")
    public Result<PostVO> detail(@PathVariable Long id) {
        return postService.detail(id);
    }

    @PutMapping("/{id}")
    public Result<Post> update(@PathVariable Long id, @RequestBody Post post, Authentication auth) {
        return postService.update(id, post, (Long) auth.getPrincipal());
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, Authentication auth) {
        return postService.delete(id, (Long) auth.getPrincipal());
    }
}
