package com.babydiary.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("growth_record")
public class GrowthRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long babyId;
    private LocalDate recordDate;
    private java.math.BigDecimal height;
    private java.math.BigDecimal weight;
    private java.math.BigDecimal headCircumference;
    private String note;
}
