package com.babydiary.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sleep_record")
public class SleepRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long babyId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer quality;
    private String note;
}
