package com.babydiary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.babydiary.common.Result;
import com.babydiary.dto.BabyDTO;
import com.babydiary.entity.Baby;
import com.babydiary.mapper.BabyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BabyService {

    private final BabyMapper babyMapper;

    public Result<Baby> create(BabyDTO dto, Long userId) {
        Baby baby = new Baby();
        baby.setUserId(userId);
        baby.setName(dto.getName());
        baby.setGender(dto.getGender());
        baby.setBirthday(dto.getBirthday());
        babyMapper.insert(baby);
        return Result.ok(baby);
    }

    public Result<List<Baby>> list(Long userId) {
        List<Baby> list = babyMapper.selectList(
                new LambdaQueryWrapper<Baby>().eq(Baby::getUserId, userId)
        );
        return Result.ok(list);
    }

    public Result<Baby> update(Long id, BabyDTO dto, Long userId) {
        Baby baby = babyMapper.selectById(id);
        if (baby == null) {
            return Result.error("宝宝不存在");
        }
        if (!baby.getUserId().equals(userId)) {
            return Result.error(403, "无权操作");
        }
        baby.setName(dto.getName());
        baby.setGender(dto.getGender());
        baby.setBirthday(dto.getBirthday());
        babyMapper.updateById(baby);
        return Result.ok(baby);
    }

    public Baby getById(Long id) {
        return babyMapper.selectById(id);
    }
}
