package ru.liga.serverfortgtinder.service;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.liga.serverfortgtinder.mappers.*;
import ru.liga.serverfortgtinder.model.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpringJdbcConnectionProvider {

    private final JdbcTemplate jdbcTemplate;
    private final OutputLikedUserMapper outputLikedUserMapper;
    private final SubstringsMapper substringsMapper;
    private final NamesMapper namesMapper;
    private final OutputUserMapper outputUserMapper;
    private final OutputUserIdMapper outputUserIdMapper;
    private final UserInfoMapper userInfoMapper;

    public SpringJdbcConnectionProvider(JdbcTemplate jdbcTemplate, OutputLikedUserMapper outputLikedUserMapper, SubstringsMapper substringsMapper, NamesMapper namesMapper, OutputUserMapper outputUserMapper, OutputUserIdMapper outputUserIdMapper, UserInfoMapper userInfoMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.outputLikedUserMapper = outputLikedUserMapper;
        this.substringsMapper = substringsMapper;
        this.namesMapper = namesMapper;
        this.outputUserMapper = outputUserMapper;
        this.outputUserIdMapper = outputUserIdMapper;
        this.userInfoMapper = userInfoMapper;
    }

    @SneakyThrows
    public int addOriginalUserInfo(UserEntity userEntity) {
        return jdbcTemplate.update(
                "INSERT INTO tinder.users(userid, chatid, gender, name, header, description, searchgender, createdt) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
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
                userEntity.getNameOldSlavonic(),
                userEntity.getHeaderOldSlavonic(),
                userEntity.getDescriptionOldSlavonic(),
                userEntity.getSearchGender(),
                userEntity.getPhoto(),
                new Timestamp(System.currentTimeMillis()));
    }

    @Bean
    public List<Substring> getSubstringsList() {
        String sql = "select * from tinder.dict_substring";
        List<Substring> resList = jdbcTemplate.query(sql, substringsMapper);
        return resList;
    }

    @Bean
    public List<OldSlavonicName> getNamesList() {
        String sql = "select * from tinder.dict_names";
        List<OldSlavonicName> resList = jdbcTemplate.query(sql, namesMapper);
        return resList;
    }

    public UserDto getNextProfile(Long chatid, String userId, Long currentProfileId) {
        String sql = "select * from tinder.users_for_view where id = (select min(id) from tinder.users_for_view where id > ? and userid != ? and gender like (select case when searchgender = 'Всех' then 'Суд%' else searchgender end from tinder.users_for_view where userid = ?) limit 1)";
        List<UserDto> userDto = jdbcTemplate.query(sql, outputUserMapper, currentProfileId, userId, userId);
        userDto.get(0).setUserId(userId);
        userDto.get(0).setChatId(chatid);
        return userDto.get(0);
    }

    public UserDto getPreviouslyProfile(Long chatid, String userId, Long currentProfileId) {
        String sql = "select * from tinder.users_for_view where id = (select max(id) from tinder.users_for_view where id < ? and userid != ? and gender like (select case when searchgender = 'Всех' then 'Суд%' else searchgender end from tinder.users_for_view where userid = ?) limit 1)";
        List<UserDto> userDto = jdbcTemplate.query(sql, outputUserMapper, currentProfileId, userId, userId);
        userDto.get(0).setUserId(userId);
        userDto.get(0).setChatId(chatid);
        return userDto.get(0);
    }

    public int addUserLike(UserLikeEntity userLikeEntity) {
        return jdbcTemplate.update(
                "INSERT INTO tinder.users_likes(userid, chatid, liked_userid, createdt) VALUES (?, ?, ?, ?)",
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
                    user.setHowLiked(1);
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
                    user.setHowLiked(2);
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
                    user.setHowLiked(3);
                    return user;
                })
                .collect(Collectors.toList());
    }

    public UserDto getUserId(Long chatId) {
        String sql = "select userid from tinder.users where chatid = ?)";
        return jdbcTemplate.query(sql, outputUserIdMapper, chatId).get(0);

    }


    public UserDto getUserInfo(String userId) {
        String sql = "select userid, chatid, gender, name, header, description, searchgender from tinder.users where userid = ?)";
        return jdbcTemplate.query(sql, userInfoMapper, userId).get(0);
    }


    public int updateOriginalUserInfo(UserEntity userEntity) {
        return jdbcTemplate.update(
                "UPDATE tinder.users SET chatid = ?, gender = ?, name = ?, header = ?, description = ?, searchgender = ?  where userid = ?",
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
                userEntity.getNameOldSlavonic(),
                userEntity.getHeaderOldSlavonic(),
                userEntity.getDescriptionOldSlavonic(),
                userEntity.getSearchGender(),
                userEntity.getPhoto(),
                userEntity.getUserId());
    }


}
