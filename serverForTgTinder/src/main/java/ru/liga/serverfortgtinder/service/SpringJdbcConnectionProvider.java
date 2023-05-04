package ru.liga.serverfortgtinder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.liga.serverfortgtinder.mappers.*;
import ru.liga.serverfortgtinder.model.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpringJdbcConnectionProvider {

    public static final int HOW_LIKED_USER = 2;
    public static final int USER_LIKED = 1;
    public static final int MUTAL_LIKE = 3;
    private final JdbcTemplate jdbcTemplate;
    private final OutputLikedUserMapper outputLikedUserMapper;
    private final OutputUserMapper outputUserMapper;
    private final OutputUserIdMapper outputUserIdMapper;
    private final UserInfoMapper userInfoMapper;


    public int addOriginalUserInfo(UserEntity userEntity) {
        return jdbcTemplate.update(
                "INSERT INTO tinder.users(userid, chatId, gender, name, header, description, searchgender, createdt) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                userEntity.getUserId(),
                userEntity.getChatId(),
                userEntity.getGender(),
                userEntity.getName(),
                userEntity.getHeader(),
                userEntity.getDescription(),
                userEntity.getSearchGender(),
                new Timestamp(System.currentTimeMillis()));
    }

    public int addModifiedUserInfo(UserEntity userEntity) {
        return jdbcTemplate.update(
                "INSERT INTO tinder.users_for_view(userid, gender, name, header, description, searchgender, photo, createdt) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                userEntity.getUserId(),
                userEntity.getGender(),
                userEntity.getNamePreReformRu(),
                userEntity.getHeaderPreReformRu(),
                userEntity.getDescriptionPreReformRu(),
                userEntity.getSearchGender(),
                userEntity.getPhoto(),
                new Timestamp(System.currentTimeMillis()));
    }


    public UserDto getNextProfile(Integer chatId, String userId, Integer currentProfileId) {
        String sql = "select * from tinder.users_for_view where id = (select min(id) from tinder.users_for_view where id > ? and userid != ? and gender like (select case when searchgender = 'Всех' then 'Суд%' else searchgender end from tinder.users_for_view where userid = ?) limit 1)";
        List<UserDto> userDto = jdbcTemplate.query(sql, outputUserMapper, currentProfileId, userId, userId);
        userDto.get(0).setUserId(userId);
        userDto.get(0).setChatId(chatId);
        return userDto.get(0);
    }

    public UserDto getPreviouslyProfile(Integer chatId, String userId, Integer currentProfileId) {
        String sql = "select * from tinder.users_for_view where id = (select max(id) from tinder.users_for_view where id < ? and userid != ? and gender like (select case when searchgender = 'Всех' then 'Суд%' else searchgender end from tinder.users_for_view where userid = ?) limit 1)";
        List<UserDto> userDto = jdbcTemplate.query(sql, outputUserMapper, currentProfileId, userId, userId);
        userDto.get(0).setUserId(userId);
        userDto.get(0).setChatId(chatId);
        return userDto.get(0);
    }

    public int addUserLike(UserLikeEntity userLikeEntity) {
        return jdbcTemplate.update(
                "INSERT INTO tinder.users_likes(userid, chatId, liked_userid, createdt) VALUES (?, ?, ?, ?)",
                userLikeEntity.getUserId(),
                userLikeEntity.getChatId(),
                userLikeEntity.getLikedUserId(),
                new Timestamp(System.currentTimeMillis()));
    }


    public List<UserDto> getUserLikes(String userId, List<String> userAddedInListLikes) {
        String sql = "select b.userid, b.name, b.gender, b.photo from tinder.users_likes a join tinder.users_for_view b on a.liked_userid = b.userid where a.userid = ?";
        List<UserDto> userDto = jdbcTemplate.query(sql, outputLikedUserMapper, userId);
        return userDto.stream()
                .filter(user -> !userAddedInListLikes.contains(user.getUserIdProfile()))
                .map(user -> {
                    user.setUserId(userId);
                    user.setHowLiked(USER_LIKED);
                    return user;
                })
                .collect(Collectors.toList());
    }

    public List<UserDto> getLikesForUser(String userId, List<String> userAddedInListLikes) {
        String sql = "select b.userid, b.name, b.gender, b.photo from tinder.users_likes a join tinder.users_for_view b on a.userid = b.userid where a.liked_userid = ?";
        List<UserDto> userDto = jdbcTemplate.query(sql, outputLikedUserMapper, userId);
        return userDto.stream()
                .filter(user -> !userAddedInListLikes.contains(user.getUserIdProfile()))
                .map(user -> {
                    user.setUserId(userId);
                    user.setHowLiked(HOW_LIKED_USER);
                    return user;
                })
                .collect(Collectors.toList());
    }

    public List<UserDto> getMutualLikeUsers(String userId) {
        String sql = "select userid, name, gender, photo from tinder.users_mutual_like where userid_main = ?;";
        List<UserDto> userDto = jdbcTemplate.query(sql, outputLikedUserMapper, userId);
        return userDto.stream()
                .map(user -> {
                    user.setUserId(userId);
                    user.setHowLiked(MUTAL_LIKE);
                    return user;
                })
                .collect(Collectors.toList());
    }

    public UserDto getUserId(Integer chatId) {
        String sql = "select userid from tinder.users where chatId = ?)";
        return jdbcTemplate.query(sql, outputUserIdMapper, chatId).get(0);

    }


    public UserDto getUserInfo(String userId) {
        String sql = "select userid, chatId, gender, name, header, description, searchgender from tinder.users where userid = ?)";
        return jdbcTemplate.query(sql, userInfoMapper, userId).get(0);
    }


    public int updateOriginalUserInfo(UserEntity userEntity) {
        return jdbcTemplate.update(
                "UPDATE tinder.users SET chatId = ?, gender = ?, name = ?, header = ?, description = ?, searchgender = ?  where userid = ?",
                userEntity.getChatId(),
                userEntity.getGender(),
                userEntity.getName(),
                userEntity.getHeader(),
                userEntity.getDescription(),
                userEntity.getSearchGender(),
                userEntity.getUserId());
    }

    public int updateModifiedUserInfo(UserEntity userEntity) {
        return jdbcTemplate.update(
                "INSERT INTO tinder.users_for_view gender = ?, name = ?, header = ?, description = ?, searchgender = ?, photo = ? where userid = ?",
                userEntity.getGender(),
                userEntity.getNamePreReformRu(),
                userEntity.getHeaderPreReformRu(),
                userEntity.getDescriptionPreReformRu(),
                userEntity.getSearchGender(),
                userEntity.getPhoto(),
                userEntity.getUserId());
    }


}
