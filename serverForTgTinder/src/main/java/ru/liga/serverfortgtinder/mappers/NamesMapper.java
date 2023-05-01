package ru.liga.serverfortgtinder.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.liga.serverfortgtinder.model.OldSlavonicName;

import java.sql.ResultSet;
import java.sql.SQLException;
@Repository
public class NamesMapper implements RowMapper<OldSlavonicName> {
    @Override
    public OldSlavonicName mapRow(ResultSet rs, int rowNum) throws SQLException {
        OldSlavonicName oldSlavonicName = new OldSlavonicName();
        oldSlavonicName.setName(rs.getString("name"));
        return oldSlavonicName;
    }
}
