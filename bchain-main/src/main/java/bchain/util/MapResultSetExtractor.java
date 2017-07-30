package bchain.util;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class MapResultSetExtractor<K, V> implements ResultSetExtractor<Map<K, V>> {
    private final RowMapper<K> keyMapper;

    private final RowMapper<V> valueMapper;

    @Override
    public Map<K, V> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<K, V> map = new HashMap<>();
        int row = 0;
        while (rs.next()) {
            row++;
            map.put(
                    keyMapper.mapRow(rs, row),
                    valueMapper.mapRow(rs, row)
            );
        }
        return map;
    }
}
