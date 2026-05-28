package com.babydiary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.babydiary.common.JwtUtils;
import com.babydiary.common.Result;
import com.babydiary.dto.LoginDTO;
import com.babydiary.dto.RegisterDTO;
import com.babydiary.entity.AiChatHistory;
import com.babydiary.entity.Diary;
import com.babydiary.entity.Bookmark;
import com.babydiary.entity.LikeRecord;
import com.babydiary.entity.Post;
import com.babydiary.entity.User;
import com.babydiary.entity.VideoMoment;
import com.babydiary.entity.Friend;
import com.babydiary.mapper.AiChatHistoryMapper;
import com.babydiary.mapper.DiaryMapper;
import com.babydiary.mapper.BookmarkMapper;
import com.babydiary.mapper.FriendMapper;
import com.babydiary.mapper.LikeRecordMapper;
import com.babydiary.mapper.PostMapper;
import com.babydiary.mapper.UserMapper;
import com.babydiary.mapper.VideoMomentMapper;
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
    private final PostMapper postMapper;
    private final VideoMomentMapper videoMomentMapper;
    private final AiChatHistoryMapper aiChatHistoryMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final BookmarkMapper bookmarkMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final FriendService friendService;
    private final FriendMapper friendMapper;

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

    public Result<UserVO> getProfile(Long userId, Long viewerId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        long diaryCount = diaryMapper.selectCount(
                new LambdaQueryWrapper<Diary>().eq(Diary::getUserId, userId));
        long momentCount = videoMomentMapper.selectCount(
                new LambdaQueryWrapper<VideoMoment>().eq(VideoMoment::getUserId, userId));
        long postCount = postMapper.selectCount(
                new LambdaQueryWrapper<Post>().eq(Post::getUserId, userId));
        long aiChatCount = aiChatHistoryMapper.selectCount(
                new LambdaQueryWrapper<AiChatHistory>().eq(AiChatHistory::getUserId, userId));
        long likeCount = likeRecordMapper.selectCount(
                new LambdaQueryWrapper<LikeRecord>().eq(LikeRecord::getUserId, userId));
        long bookmarkCount = bookmarkMapper.selectCount(
                new LambdaQueryWrapper<Bookmark>().eq(Bookmark::getUserId, userId));
        int friendCount = friendService.getFriendIds(userId).size();
        boolean isFriend = viewerId != null && !viewerId.equals(userId) && friendService.isFriend(viewerId, userId);
        Integer friendStatus = null;
        if (viewerId != null && !viewerId.equals(userId)) {
            if (isFriend) {
                friendStatus = 1;
            } else {
                long pendingCount = friendMapper.selectCount(new LambdaQueryWrapper<Friend>()
                        .eq(Friend::getUserId, viewerId)
                        .eq(Friend::getFriendId, userId)
                        .eq(Friend::getStatus, 0));
                if (pendingCount > 0) friendStatus = 0;
            }
        }

        UserVO vo = UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .diaryCount((int) diaryCount)
                .momentCount((int) momentCount)
                .postCount((int) postCount)
                .aiChatCount((int) aiChatCount)
                .likeCount((int) likeCount)
                .bookmarkCount((int) bookmarkCount)
                .friendCount(friendCount)
                .isFriend(isFriend)
                .friendStatus(friendStatus)
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
