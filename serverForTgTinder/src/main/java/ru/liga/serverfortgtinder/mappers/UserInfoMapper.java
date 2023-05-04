package ru.liga.serverfortgtinder.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.liga.serverfortgtinder.model.UserDto;

import java.sql.ResultSet;
import java.sql.SQLException;
@Repository
public class UserInfoMapper implements RowMapper<UserDto> {
    @Override
    public UserDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        UserDto userDto = new UserDto();
        userDto.setUserId(rs.getString("userid"));
        userDto.setChatId(rs.getInt("chatid"));
        userDto.setGender(rs.getString("gender"));
        userDto.setName(rs.getString("name"));
        userDto.setHeader(rs.getString("header"));
        userDto.setDescription(rs.getString("description"));
        userDto.setSearchGender(rs.getString("searchgender"));
        return userDto;
    }
}
