package bchain.util;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.function.BinaryOperator;

public class ExtendedJdbcTemplate extends JdbcTemplate {
    public ExtendedJdbcTemplate() {
    }

    public ExtendedJdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }

    public ExtendedJdbcTemplate(DataSource dataSource, boolean lazyInit) {
        super(dataSource, lazyInit);
    }

    public <T> T batchQuery(final String sql,
                            BatchPreparedStatementSetter pss,
                            final ResultSetExtractor<T> rse,
                            BinaryOperator<T> combiner) throws DataAccessException {

        return execute(sql, (PreparedStatementCallback<T>) ps -> {
            try {
                int batchSize = pss.getBatchSize();
                InterruptibleBatchPreparedStatementSetter ipss =
                        (pss instanceof InterruptibleBatchPreparedStatementSetter ?
                                (InterruptibleBatchPreparedStatementSetter) pss : null);
                if (JdbcUtils.supportsBatchUpdates(ps.getConnection())) {
                    T result = null;
                    for (int i = 0; i < batchSize; i++) {
                        pss.setValues(ps, i);
                        if (ipss != null && ipss.isBatchExhausted(i)) {
                            break;
                        }
                        ResultSet rs = ps.executeQuery();
                        T data = rse.extractData(rs);
                        if (result == null) {
                            result = data;
                        } else {
                            result = combiner.apply(result, data);
                        }
                    }
                    return result;
                } else {
                    T result = null;
                    for (int i = 0; i < batchSize; i++) {
                        pss.setValues(ps, i);
                        if (ipss != null && ipss.isBatchExhausted(i)) {
                            break;
                        }
                        ResultSet rs = ps.executeQuery();
                        T data = rse.extractData(rs);
                        if (result == null) {
                            result = data;
                        } else {
                            result = combiner.apply(result, data);
                        }
                    }
                    return result;
                }
            } finally {
                if (pss instanceof ParameterDisposer) {
                    ((ParameterDisposer) pss).cleanupParameters();
                }
            }
        });
    }
}
