package com.babydiary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.babydiary.common.PageResult;
import com.babydiary.common.Result;
import com.babydiary.dto.DiaryDTO;
import com.babydiary.entity.*;
import com.babydiary.mapper.*;
import com.babydiary.vo.DiaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryMapper diaryMapper;
    private final DiaryImageMapper diaryImageMapper;
    private final UserMapper userMapper;
    private final BabyMapper babyMapper;

    @Transactional
    public Result<Diary> create(DiaryDTO dto, Long userId) {
        Diary diary = new Diary();
        diary.setUserId(userId);
        diary.setBabyId(dto.getBabyId());
        diary.setTitle(dto.getTitle());
        diary.setContent(dto.getContent());
        diary.setVisibility(dto.getVisibility() != null ? dto.getVisibility() : 1);
        diaryMapper.insert(diary);

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (int i = 0; i < dto.getImages().size(); i++) {
                DiaryImage img = new DiaryImage();
                img.setDiaryId(diary.getId());
                img.setUrl(dto.getImages().get(i));
                img.setSort(i);
                diaryImageMapper.insert(img);
            }
        }
        return Result.ok(diary);
    }

    public Result<PageResult<DiaryVO>> list(int page, int size, Long userId) {
        IPage<Diary> pageObj = diaryMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Diary>()
                        .eq(Diary::getVisibility, 1)
                        .orderByDesc(Diary::getCreatedAt)
        );
        List<DiaryVO> vos = pageObj.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        PageResult<DiaryVO> result = new PageResult<>();
        result.setTotal(pageObj.getTotal());
        result.setPages(pageObj.getPages());
        result.setCurrent(pageObj.getCurrent());
        result.setRecords(vos);
        return Result.ok(result);
    }

    public Result<DiaryVO> detail(Long id) {
        Diary diary = diaryMapper.selectById(id);
        if (diary == null) {
            return Result.error("日记不存在");
        }
        diary.setViewCount(diary.getViewCount() + 1);
        diaryMapper.updateById(diary);
        return Result.ok(toVO(diary));
    }

    @Transactional
    public Result<String> delete(Long id, Long userId) {
        Diary diary = diaryMapper.selectById(id);
        if (diary == null) {
            return Result.error("日记不存在");
        }
        if (!diary.getUserId().equals(userId)) {
            return Result.error(403, "无权操作");
        }
        diaryImageMapper.delete(new LambdaQueryWrapper<DiaryImage>().eq(DiaryImage::getDiaryId, id));
        diaryMapper.deleteById(id);
        return Result.ok("删除成功");
    }

    private DiaryVO toVO(Diary diary) {
        User user = userMapper.selectById(diary.getUserId());
        String babyName = null;
        if (diary.getBabyId() != null) {
            Baby baby = babyMapper.selectById(diary.getBabyId());
            babyName = baby != null ? baby.getName() : null;
        }
        List<String> images = diaryImageMapper.selectList(
                new LambdaQueryWrapper<DiaryImage>()
                        .eq(DiaryImage::getDiaryId, diary.getId())
                        .orderByAsc(DiaryImage::getSort)
        ).stream().map(DiaryImage::getUrl).collect(Collectors.toList());

        return DiaryVO.builder()
                .id(diary.getId())
                .userId(diary.getUserId())
                .authorName(user != null ? user.getUsername() : "未知")
                .authorAvatar(user != null ? user.getAvatar() : null)
                .babyId(diary.getBabyId())
                .babyName(babyName)
                .title(diary.getTitle())
                .content(diary.getContent())
                .visibility(diary.getVisibility())
                .viewCount(diary.getViewCount())
                .images(images)
                .createdAt(diary.getCreatedAt())
                .build();
    }
}
