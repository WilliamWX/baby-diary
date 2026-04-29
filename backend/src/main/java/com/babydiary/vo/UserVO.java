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
    private int followerCount;
    private int followingCount;
    private LocalDateTime createdAt;
}
