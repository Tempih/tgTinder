package ru.liga.serverfortgtinder.service;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import ru.liga.serverfortgtinder.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpringJdbcConnectionProvider {

    @Autowired
    private JdbcTemplate jdbcTemplate;
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

        SubstringsMapper mapper = new SubstringsMapper();
        List<Substring> resList = jdbcTemplate.query(sql, mapper);
        return resList;
    }

    private static class SubstringsMapper implements RowMapper<Substring> {

        @Override
        public Substring mapRow(ResultSet rs, int rowNum) throws SQLException {
            Substring substring = new Substring();
            substring.setRuSubstring(rs.getString("ru_substring"));
            substring.setSsSubstring(rs.getString("ss_substring"));
            return substring;
        }
    }

    private static class NamesMapper implements RowMapper<OldSlavonicName> {
        @Override
        public OldSlavonicName mapRow(ResultSet rs, int rowNum) throws SQLException {
            OldSlavonicName oldSlavonicName = new OldSlavonicName();
            oldSlavonicName.setName(rs.getString("name"));
            return oldSlavonicName;
        }
    }

    @Bean
    public List<OldSlavonicName> getNamesList() {
        String sql = "select * from tinder.dict_names";

        NamesMapper mapper = new NamesMapper();
        List<OldSlavonicName> resList = jdbcTemplate.query(sql, mapper);
        return resList;
    }


    private static class OutputUserMapper implements RowMapper<UserDto> {
        @Override
        public UserDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserDto userDto = new UserDto();
            userDto.setUserIdProfile(rs.getString("userid"));
            userDto.setName(rs.getString("name"));
            userDto.setGender(rs.getString("gender"));
            userDto.setPhoto(rs.getString("photo"));
            userDto.setCurrentProfileId((rs.getInt("id")));
            return userDto;
        }
    }

    public UserDto getNextProfile(Long chatid, String userId, Long currentProfileId) {
        String sql = "select * from tinder.users_for_view where id = (select min(id) from tinder.users_for_view where id > ? and userid != ? and gender like (select case when searchgender = 'Всех' then 'Суд%' else searchgender end from tinder.users_for_view where userid = ?) limit 1)";
        OutputUserMapper mapper = new OutputUserMapper();
        List<UserDto> userDto = jdbcTemplate.query(sql, mapper, currentProfileId, userId, userId);
        userDto.get(0).setUserId(userId);
        userDto.get(0).setChatId(chatid);
        return userDto.get(0);
    }

    public UserDto getPreviouslyProfile(Long chatid, String userId, Long currentProfileId) {
        String sql = "select * from tinder.users_for_view where id = (select max(id) from tinder.users_for_view where id < ? and userid != ? and gender like (select case when searchgender = 'Всех' then 'Суд%' else searchgender end from tinder.users_for_view where userid = ?) limit 1)";
        OutputUserMapper mapper = new OutputUserMapper();
        List<UserDto> userDto = jdbcTemplate.query(sql, mapper, currentProfileId, userId, userId);
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


    private static class OutputLikedUserMapper implements RowMapper<UserDto> {
        @Override
        public UserDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserDto userDto = new UserDto();
            userDto.setUserIdProfile(rs.getString("userid"));
            userDto.setName(rs.getString("name"));
            userDto.setGender(rs.getString("gender"));
            userDto.setPhoto(rs.getString("photo"));
            return userDto;
        }
    }

    public List<UserDto> getUserLikes(String userId, List<String> userAddedInListLikes) {
        String sql = "select b.userid, b.name, b.gender, b.photo from tinder.users_likes a join tinder.users_for_view b on a.liked_userid = b.userid where a.userid = ?";
        OutputLikedUserMapper mapper = new OutputLikedUserMapper();
        List<UserDto> userDto = jdbcTemplate.query(sql, mapper, userId);
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
        OutputLikedUserMapper mapper = new OutputLikedUserMapper();
        List<UserDto> userDto = jdbcTemplate.query(sql, mapper, userId);
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
        OutputLikedUserMapper mapper = new OutputLikedUserMapper();
        List<UserDto> userDto = jdbcTemplate.query(sql, mapper, userId);
        return userDto.stream()
                .map(user -> {
                    user.setUserId(userId);
                    user.setHowLiked(3);
                    return user;
                })
                .collect(Collectors.toList());
    }

    public UserDto getUserId(Long chatId){
        String sql = "select userid from tinder.users where chatid = ?)";
        OutputUserIdMapper mapper = new OutputUserIdMapper();
        return jdbcTemplate.query(sql, mapper, chatId).get(0);

    }

    private static class OutputUserIdMapper implements RowMapper<UserDto> {
        @Override
        public UserDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserDto userDto = new UserDto();
            userDto.setUserId(rs.getString("userid"));
            return userDto;
        }
    }

    public UserDto getUserInfo(String userId){
        String sql = "select userid, chatid, gender, name, header, description, searchgender from tinder.users where userid = ?)";
        UserInfoMapper mapper = new UserInfoMapper();
        return jdbcTemplate.query(sql, mapper, userId).get(0);
    }

    private static class UserInfoMapper implements RowMapper<UserDto> {
        @Override
        public UserDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserDto userDto = new UserDto();
            userDto.setUserId(rs.getString("userid"));
            userDto.setChatId(rs.getLong("chatid"));
            userDto.setGender(rs.getString("gender"));
            userDto.setName(rs.getString("name"));
            userDto.setHeader(rs.getString("header"));
            userDto.setDescription(rs.getString("description"));
            userDto.setSearchGender(rs.getString("searchgender"));
            return userDto;
        }
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
