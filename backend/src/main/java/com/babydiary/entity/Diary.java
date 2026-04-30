package com.babydiary.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("diary")
public class Diary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long babyId;
    private String content;
    private Integer visibility;
    private Integer viewCount;
    private LocalDate recordDate;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
