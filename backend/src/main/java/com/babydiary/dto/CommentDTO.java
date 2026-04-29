package com.babydiary.dto;

import lombok.Data;

@Data
public class CommentDTO {
    private String targetType;
    private Long targetId;
    private Long parentId;
    private String content;
}
