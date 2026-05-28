package com.babydiary.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VideoMomentVO {
    private Long id;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private Long babyId;
    private String babyName;
    private String description;
    private String videoUrl;
    private String coverUrl;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean liked;
    private Boolean bookmarked;
    private Integer visibility;
    private LocalDateTime createdAt;
}
