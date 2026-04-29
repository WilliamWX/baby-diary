package com.babydiary.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("feeding_record")
public class FeedingRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long babyId;
    private LocalDateTime feedTime;
    private Integer type;
    private String amount;
    private String foodName;
    private String note;
}
