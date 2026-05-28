package com.babydiary.dto;

import lombok.Data;

@Data
public class VideoMomentDTO {
    private String description;
    private Long babyId;
    private String videoUrl;
    private String coverUrl;
    private Integer visibility;
    private String visibleTo;
}
