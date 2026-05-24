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

    public Result<PageResult<VideoMomentVO>> list(int page, int size, Long babyId, String keyword, String sort, Long userId) {
        LambdaQueryWrapper<VideoMoment> wrapper = new LambdaQueryWrapper<VideoMoment>();
        if (babyId != null) {
            wrapper.eq(VideoMoment::getBabyId, babyId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(VideoMoment::getDescription, keyword);
        }

        PageResult<VideoMomentVO> result = new PageResult<>();
        if ("popular".equals(sort)) {
            wrapper.orderByDesc(VideoMoment::getCreatedAt);
            List<VideoMoment> allMoments = videoMomentMapper.selectList(wrapper);
            List<VideoMomentVO> allVos = allMoments.stream().map(m -> toVO(m, userId)).collect(Collectors.toList());
            allVos.sort((a, b) -> Integer.compare(b.getLikeCount(), a.getLikeCount()));
            result.setTotal(allVos.size());
            result.setPages((int) Math.ceil((double) allVos.size() / size));
            result.setCurrent(page);
            int from = (page - 1) * size;
            int to = Math.min(from + size, allVos.size());
            result.setRecords(from < allVos.size() ? allVos.subList(from, to) : List.of());
        } else {
            wrapper.orderByDesc(VideoMoment::getCreatedAt);
            IPage<VideoMoment> pageObj = videoMomentMapper.selectPage(new Page<>(page, size), wrapper);
            List<VideoMomentVO> vos = pageObj.getRecords().stream().map(m -> toVO(m, userId)).collect(Collectors.toList());
            result.setTotal(pageObj.getTotal());
            result.setPages(pageObj.getPages());
            result.setCurrent(pageObj.getCurrent());
            result.setRecords(vos);
        }
        return Result.ok(result);
    }

    public Result<VideoMomentVO> detail(Long id, Long userId) {
        VideoMoment m = videoMomentMapper.selectById(id);
        if (m == null) return Result.error("精彩时刻不存在");
        m.setViewCount(m.getViewCount() + 1);
        videoMomentMapper.updateById(m);
        return Result.ok(toVO(m, userId));
    }

    @Transactional
    public Result<VideoMoment> update(Long id, VideoMomentDTO dto, Long userId) {
        VideoMoment m = videoMomentMapper.selectById(id);
        if (m == null) return Result.error("精彩时刻不存在");
        if (!m.getUserId().equals(userId)) return Result.error(403, "无权操作");
        m.setDescription(dto.getDescription());
        m.setBabyId(dto.getBabyId());
        if (dto.getVideoUrl() != null) {
            m.setVideoUrl(dto.getVideoUrl());
        }
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

    private VideoMomentVO toVO(VideoMoment m, Long userId) {
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
                .liked(interactService.isLiked(userId, "moment", m.getId()))
                .bookmarked(interactService.isBookmarked(userId, "moment", m.getId()))
                .createdAt(m.getCreatedAt())
                .build();
    }
}
