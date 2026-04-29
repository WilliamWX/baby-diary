package com.babydiary.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("milestone")
public class Milestone {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long babyId;
    private String title;
    private String content;
    private LocalDate eventDate;
    private String photos;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
