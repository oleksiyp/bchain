package bchain.dao.sqlite;

import bchain.dao.PendingTxDao;
import bchain.domain.Hash;
import bchain.util.ExtendedJdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.IntStream;

import static bchain.domain.Hash.hash;
import static java.util.stream.IntStream.of;

public class SqlitePendingTxDao implements PendingTxDao {
    @Autowired
    ExtendedJdbcTemplate jdbcTemplate;

    @Override
    public void markPending(List<Hash> txs) {
        jdbcTemplate.batchUpdate("insert into PendingTx(hash) values (?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Hash hash = txs.get(i);
                        ps.setBytes(1, hash.getValues());
                    }

                    @Override
                    public int getBatchSize() {
                        return txs.size();
                    }
                });
    }

    @Override
    public void unmarkPending(List<Hash> txs) {
        int[] nDeleted = jdbcTemplate.batchUpdate("delete from PendingTx where hash = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Hash hash = txs.get(i);
                        ps.setBytes(1, hash.getValues());
                    }

                    @Override
                    public int getBatchSize() {
                        return txs.size();
                    }
                });
        if (!of(nDeleted).allMatch(deleted -> deleted == 1)) {
            throw new RuntimeException("not all pending transactions deleted");
        }
    }

    @Override
    public List<Hash> all() {
        return jdbcTemplate.query("select hash from PendingTx",
                (rs, i) -> hash(rs.getBytes(1)));
    }
}
