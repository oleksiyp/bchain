package bchain.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtendedJdbcTemplate extends JdbcTemplate {
    public ExtendedJdbcTemplate() {
    }

    public ExtendedJdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }

    public ExtendedJdbcTemplate(DataSource dataSource, boolean lazyInit) {
        super(dataSource, lazyInit);
    }

    public <K, V> Map<K, V> queryMapSingleValue(String sql,
                                                List<K> params,
                                                PrepareMapper<K> prepareMapper,
                                                RowMapper<V> rowMapper) {
        return execute(sql, (PreparedStatementCallback<Map<K, V>>) ps -> {
            Map<K, V> map = new HashMap<>();
            int n = 0;
            for (K param : params) {
                prepareMapper.prepare(ps, n++, param);
                ResultSet rs = ps.executeQuery();
                int rowNum = 0;
                while (rs.next()) {
                    map.put(param, rowMapper.mapRow(rs, ++rowNum));
                }
            }
            return map;
        });
    }


    public <K, V> Map<K, List<V>> queryMapList(String sql,
                                               List<K> params,
                                               PrepareMapper<K> keyMapper,
                                               RowMapper<V> rowMapper) {
        return execute(sql, (PreparedStatementCallback<Map<K, List<V>>>) ps -> {
            Map<K, List<V>> map = new HashMap<>();
            int n = 0;
            for (K param : params) {
                keyMapper.prepare(ps, n++, param);
                ResultSet rs = ps.executeQuery();
                List<V> lst = new ArrayList<>();
                int rowNum = 0;
                while (rs.next()) {
                    lst.add(rowMapper.mapRow(rs, ++rowNum));
                }
                map.put(param, lst);
            }
            return map;
        });
    }

}
