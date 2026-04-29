package com.babydiary.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("diary_image")
public class DiaryImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long diaryId;
    private String url;
    private Integer sort;
}
