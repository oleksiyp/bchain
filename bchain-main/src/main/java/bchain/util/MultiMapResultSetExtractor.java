package bchain.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class MultiMapResultSetExtractor<K, V> implements ResultSetExtractor<Multimap<K, V>> {
    private final RowMapper<K> keyMapper;

    private final RowMapper<V> valueMapper;

    @Override
    public Multimap<K, V> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Multimap<K, V> map = HashMultimap.create();
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
