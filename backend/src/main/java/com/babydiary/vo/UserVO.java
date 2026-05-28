package com.babydiary.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String bio;
    private int diaryCount;
    private int momentCount;
    private int postCount;
    private int aiChatCount;
    private int likeCount;
    private int bookmarkCount;
    private int friendCount;
    private boolean isFriend;
    private Integer friendStatus;
    private LocalDateTime createdAt;
}
