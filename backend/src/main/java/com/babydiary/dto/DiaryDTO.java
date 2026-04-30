package com.babydiary.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DiaryDTO {
    private String content;
    private Long babyId;
    private Integer visibility;
    private LocalDate recordDate;
    private List<String> images;
}
