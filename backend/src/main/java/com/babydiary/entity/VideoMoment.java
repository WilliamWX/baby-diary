package com.babydiary.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("video_moment")
public class VideoMoment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long babyId;
    private String description;
    private String videoUrl;
    private String coverUrl;
    private Integer viewCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
