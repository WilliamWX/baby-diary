package com.babydiary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.babydiary.common.PageResult;
import com.babydiary.common.Result;
import com.babydiary.dto.VideoMomentDTO;
import com.babydiary.entity.Baby;
import com.babydiary.entity.User;
import com.babydiary.entity.VideoMoment;
import com.babydiary.mapper.BabyMapper;
import com.babydiary.mapper.UserMapper;
import com.babydiary.mapper.VideoMomentMapper;
import com.babydiary.vo.VideoMomentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoMomentService {

    private final VideoMomentMapper videoMomentMapper;
    private final UserMapper userMapper;
    private final BabyMapper babyMapper;
    private final InteractService interactService;

    @Transactional
    public Result<VideoMoment> create(VideoMomentDTO dto, Long userId) {
        if (dto.getVideoUrl() == null || dto.getVideoUrl().isEmpty()) {
            return Result.error("请先上传视频");
        }
        VideoMoment m = new VideoMoment();
        m.setUserId(userId);
        m.setBabyId(dto.getBabyId());
        m.setDescription(dto.getDescription());
        m.setVideoUrl(dto.getVideoUrl());
        m.setCoverUrl(dto.getCoverUrl());
        m.setViewCount(0);
        videoMomentMapper.insert(m);
        return Result.ok(m);
    }

    public Result<PageResult<VideoMomentVO>> list(int page, int size, Long babyId, String keyword) {
        LambdaQueryWrapper<VideoMoment> wrapper = new LambdaQueryWrapper<VideoMoment>()
                .orderByDesc(VideoMoment::getCreatedAt);
        if (babyId != null) {
            wrapper.eq(VideoMoment::getBabyId, babyId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(VideoMoment::getDescription, keyword);
        }
        IPage<VideoMoment> pageObj = videoMomentMapper.selectPage(new Page<>(page, size), wrapper);
        List<VideoMomentVO> vos = pageObj.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        PageResult<VideoMomentVO> result = new PageResult<>();
        result.setTotal(pageObj.getTotal());
        result.setPages(pageObj.getPages());
        result.setCurrent(pageObj.getCurrent());
        result.setRecords(vos);
        return Result.ok(result);
    }

    public Result<VideoMomentVO> detail(Long id) {
        VideoMoment m = videoMomentMapper.selectById(id);
        if (m == null) return Result.error("精彩时刻不存在");
        m.setViewCount(m.getViewCount() + 1);
        videoMomentMapper.updateById(m);
        return Result.ok(toVO(m));
    }

    @Transactional
    public Result<VideoMoment> update(Long id, VideoMomentDTO dto, Long userId) {
        VideoMoment m = videoMomentMapper.selectById(id);
        if (m == null) return Result.error("精彩时刻不存在");
        if (!m.getUserId().equals(userId)) return Result.error(403, "无权操作");
        m.setDescription(dto.getDescription());
        m.setBabyId(dto.getBabyId());
        if (dto.getCoverUrl() != null) {
            m.setCoverUrl(dto.getCoverUrl());
        }
        videoMomentMapper.updateById(m);
        return Result.ok(m);
    }

    @Transactional
    public Result<String> delete(Long id, Long userId) {
        VideoMoment m = videoMomentMapper.selectById(id);
        if (m == null) return Result.error("精彩时刻不存在");
        if (!m.getUserId().equals(userId)) return Result.error(403, "无权操作");
        videoMomentMapper.deleteById(id);
        return Result.ok("删除成功");
    }

    private VideoMomentVO toVO(VideoMoment m) {
        User user = userMapper.selectById(m.getUserId());
        String babyName = null;
        if (m.getBabyId() != null) {
            Baby baby = babyMapper.selectById(m.getBabyId());
            babyName = baby != null ? baby.getName() : null;
        }
        return VideoMomentVO.builder()
                .id(m.getId())
                .userId(m.getUserId())
                .authorName(user != null ? user.getUsername() : "未知")
                .authorAvatar(user != null ? user.getAvatar() : null)
                .babyId(m.getBabyId())
                .babyName(babyName)
                .description(m.getDescription())
                .videoUrl(m.getVideoUrl())
                .coverUrl(m.getCoverUrl())
                .viewCount(m.getViewCount())
                .likeCount((int) interactService.likeCount("moment", m.getId()))
                .commentCount((int) interactService.commentCount("moment", m.getId()))
                .createdAt(m.getCreatedAt())
                .build();
    }
}
