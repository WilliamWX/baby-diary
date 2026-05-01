package com.babydiary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.babydiary.common.JwtUtils;
import com.babydiary.common.Result;
import com.babydiary.dto.LoginDTO;
import com.babydiary.dto.RegisterDTO;
import com.babydiary.entity.Diary;
import com.babydiary.entity.Follow;
import com.babydiary.entity.User;
import com.babydiary.mapper.DiaryMapper;
import com.babydiary.mapper.FollowMapper;
import com.babydiary.mapper.UserMapper;
import com.babydiary.vo.LoginVO;
import com.babydiary.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final DiaryMapper diaryMapper;
    private final FollowMapper followMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public Result<LoginVO> register(RegisterDTO dto) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())) > 0) {
            return Result.error("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        userMapper.insert(user);

        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        return Result.ok(LoginVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .token(token)
                .build());
    }

    public Result<LoginVO> login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        return Result.ok(LoginVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .token(token)
                .build());
    }

    public Result<UserVO> getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        long diaryCount = diaryMapper.selectCount(
                new LambdaQueryWrapper<Diary>().eq(Diary::getUserId, userId));
        long followerCount = followMapper.selectCount(
                new LambdaQueryWrapper<Follow>().eq(Follow::getFollowingId, userId));
        long followingCount = followMapper.selectCount(
                new LambdaQueryWrapper<Follow>().eq(Follow::getFollowerId, userId));

        UserVO vo = UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .diaryCount((int) diaryCount)
                .followerCount((int) followerCount)
                .followingCount((int) followingCount)
                .createdAt(user.getCreatedAt())
                .build();
        return Result.ok(vo);
    }

    public void updateAvatar(Long userId, String url) {
        User user = new User();
        user.setId(userId);
        user.setAvatar(url);
        userMapper.updateById(user);
    }

    public void updateBio(Long userId, String bio) {
        User user = new User();
        user.setId(userId);
        user.setBio(bio);
        userMapper.updateById(user);
    }
}
