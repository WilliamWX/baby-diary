package com.babydiary.controller;

import com.babydiary.common.Result;
import com.babydiary.entity.*;
import com.babydiary.service.GrowthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/growth")
@RequiredArgsConstructor
public class GrowthController {

    private final GrowthService growthService;

    @PostMapping("/record")
    public Result<GrowthRecord> addRecord(@RequestBody GrowthRecord record, Authentication auth) {
        return growthService.addRecord(record, (Long) auth.getPrincipal());
    }

    @GetMapping("/record/{babyId}")
    public Result<List<GrowthRecord>> records(@PathVariable Long babyId) {
        return growthService.records(babyId);
    }

    @GetMapping("/chart/{babyId}")
    public Result<Map<String, Object>> chartData(@PathVariable Long babyId) {
        return growthService.chartData(babyId);
    }
}

@RestController
@RequestMapping("/api/v1/feeding")
@RequiredArgsConstructor
class FeedingController {

    private final GrowthService growthService;

    @PostMapping
    public Result<FeedingRecord> add(@RequestBody FeedingRecord record, Authentication auth) {
        return growthService.addFeeding(record, (Long) auth.getPrincipal());
    }

    @GetMapping("/list/{babyId}")
    public Result<List<FeedingRecord>> list(@PathVariable Long babyId, @RequestParam(required = false) String date) {
        return growthService.feedingList(babyId, date);
    }
}

@RestController
@RequestMapping("/api/v1/sleep")
@RequiredArgsConstructor
class SleepController {

    private final GrowthService growthService;

    @PostMapping
    public Result<SleepRecord> add(@RequestBody SleepRecord record, Authentication auth) {
        return growthService.addSleep(record, (Long) auth.getPrincipal());
    }

    @GetMapping("/list/{babyId}")
    public Result<List<SleepRecord>> list(@PathVariable Long babyId, @RequestParam(required = false) String date) {
        return growthService.sleepList(babyId, date);
    }
}

@RestController
@RequestMapping("/api/v1/milestone")
@RequiredArgsConstructor
class MilestoneController {

    private final GrowthService growthService;

    @PostMapping
    public Result<Milestone> add(@RequestBody Milestone milestone, Authentication auth) {
        return growthService.addMilestone(milestone, (Long) auth.getPrincipal());
    }

    @GetMapping("/list/{babyId}")
    public Result<List<Milestone>> list(@PathVariable Long babyId) {
        return growthService.milestoneList(babyId);
    }
}
