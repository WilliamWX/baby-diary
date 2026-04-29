package com.babydiary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class DiaryDTO {
    @NotBlank(message = "标题不能为空")
    private String title;
    private String content;
    private Long babyId;
    private Integer visibility;
    private List<String> images;
}
