package com.babydiary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.babydiary.common.Result;
import com.babydiary.entity.*;
import com.babydiary.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GrowthService {

    private final GrowthRecordMapper growthRecordMapper;
    private final FeedingRecordMapper feedingRecordMapper;
    private final SleepRecordMapper sleepRecordMapper;
    private final MilestoneMapper milestoneMapper;
    private final BabyMapper babyMapper;

    // === 成长记录 ===
    public Result<GrowthRecord> addRecord(GrowthRecord record, Long userId) {
        Baby baby = babyMapper.selectById(record.getBabyId());
        if (baby == null || !baby.getUserId().equals(userId)) {
            return Result.error(403, "无权操作");
        }
        growthRecordMapper.insert(record);
        return Result.ok(record);
    }

    public Result<List<GrowthRecord>> records(Long babyId) {
        List<GrowthRecord> list = growthRecordMapper.selectList(
                new LambdaQueryWrapper<GrowthRecord>()
                        .eq(GrowthRecord::getBabyId, babyId)
                        .orderByAsc(GrowthRecord::getRecordDate)
        );
        return Result.ok(list);
    }

    public Result<Map<String, Object>> chartData(Long babyId) {
        List<GrowthRecord> list = growthRecordMapper.selectList(
                new LambdaQueryWrapper<GrowthRecord>()
                        .eq(GrowthRecord::getBabyId, babyId)
                        .orderByAsc(GrowthRecord::getRecordDate)
        );
        Map<String, Object> data = new HashMap<>();
        data.put("dates", list.stream().map(r -> r.getRecordDate().toString()).collect(Collectors.toList()));
        data.put("heights", list.stream().map(GrowthRecord::getHeight).collect(Collectors.toList()));
        data.put("weights", list.stream().map(GrowthRecord::getWeight).collect(Collectors.toList()));
        data.put("headCircumferences", list.stream().map(GrowthRecord::getHeadCircumference).collect(Collectors.toList()));
        return Result.ok(data);
    }

    // === 喂养记录 ===
    public Result<FeedingRecord> addFeeding(FeedingRecord record, Long userId) {
        Baby baby = babyMapper.selectById(record.getBabyId());
        if (baby == null || !baby.getUserId().equals(userId)) {
            return Result.error(403, "无权操作");
        }
        feedingRecordMapper.insert(record);
        return Result.ok(record);
    }

    public Result<List<FeedingRecord>> feedingList(Long babyId, String date) {
        LambdaQueryWrapper<FeedingRecord> wrapper = new LambdaQueryWrapper<FeedingRecord>()
                .eq(FeedingRecord::getBabyId, babyId)
                .orderByDesc(FeedingRecord::getFeedTime);
        if (date != null) {
            wrapper.apply("DATE(feed_time) = {0}", date);
        }
        return Result.ok(feedingRecordMapper.selectList(wrapper));
    }

    // === 睡眠记录 ===
    public Result<SleepRecord> addSleep(SleepRecord record, Long userId) {
        Baby baby = babyMapper.selectById(record.getBabyId());
        if (baby == null || !baby.getUserId().equals(userId)) {
            return Result.error(403, "无权操作");
        }
        sleepRecordMapper.insert(record);
        return Result.ok(record);
    }

    public Result<List<SleepRecord>> sleepList(Long babyId, String date) {
        LambdaQueryWrapper<SleepRecord> wrapper = new LambdaQueryWrapper<SleepRecord>()
                .eq(SleepRecord::getBabyId, babyId)
                .orderByDesc(SleepRecord::getStartTime);
        if (date != null) {
            wrapper.apply("DATE(start_time) = {0}", date);
        }
        return Result.ok(sleepRecordMapper.selectList(wrapper));
    }

    // === 里程碑 ===
    public Result<Milestone> addMilestone(Milestone milestone, Long userId) {
        Baby baby = babyMapper.selectById(milestone.getBabyId());
        if (baby == null || !baby.getUserId().equals(userId)) {
            return Result.error(403, "无权操作");
        }
        milestoneMapper.insert(milestone);
        return Result.ok(milestone);
    }

    public Result<List<Milestone>> milestoneList(Long babyId) {
        List<Milestone> list = milestoneMapper.selectList(
                new LambdaQueryWrapper<Milestone>()
                        .eq(Milestone::getBabyId, babyId)
                        .orderByDesc(Milestone::getEventDate)
        );
        return Result.ok(list);
    }
}
