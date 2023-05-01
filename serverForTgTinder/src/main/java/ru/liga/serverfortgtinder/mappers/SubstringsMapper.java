package ru.liga.serverfortgtinder.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.liga.serverfortgtinder.model.Substring;

import java.sql.ResultSet;
import java.sql.SQLException;
@Repository
public class SubstringsMapper implements RowMapper<Substring> {
    @Override
    public Substring mapRow(ResultSet rs, int rowNum) throws SQLException {
        Substring substring = new Substring();
        substring.setRuSubstring(rs.getString("ru_substring"));
        substring.setSsSubstring(rs.getString("ss_substring"));
        return substring;
    }
}