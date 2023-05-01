package ru.liga.serverfortgtinder.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.liga.serverfortgtinder.model.UserDto;

import java.sql.ResultSet;
import java.sql.SQLException;
@Repository
public class OutputLikedUserMapper implements RowMapper<UserDto> {
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