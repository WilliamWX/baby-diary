package com.babydiary.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DiaryVO {
    private Long id;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private Long babyId;
    private String babyName;
    private String title;
    private String content;
    private Integer visibility;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private List<String> images;
    private LocalDateTime createdAt;
}
