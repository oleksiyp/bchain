package bchain.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.util.StopWatch;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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


//    @Override
//    public <T> T execute(StatementCallback<T> action) throws DataAccessException {
//        return super.execute((StatementCallback<T>) (stmt) -> {
//            StopWatch sw = new StopWatch();
//            sw.start();
//            T ret = action.doInStatement(stmt);
//            sw.stop();
//            log.info("{} {}", sw.getLastTaskTimeMillis(), stmt);
//            return ret;
//        });
//    }
//
//    @Override
//    public <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) throws DataAccessException {
//        return super.execute(psc, (stmt) -> {
//            StopWatch sw = new StopWatch();
//            sw.start();
//            T ret = action.doInPreparedStatement(stmt);
//            sw.stop();
//            log.info("{} {}", sw.getLastTaskTimeMillis(), stmt);
//            return ret;
//        });
//    }
//
//    @Override
//    public <T> T execute(CallableStatementCreator csc, CallableStatementCallback<T> action) throws DataAccessException {
//        return super.execute(csc, (stmt) -> {
//            StopWatch sw = new StopWatch();
//            sw.start();
//            T ret = action.doInCallableStatement(stmt);
//            sw.stop();
//            log.info("{} {}", sw.getLastTaskTimeMillis(), stmt);
//            return ret;
//        });
//    }
}
