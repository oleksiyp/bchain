package bchain.dao.sqlite;

import bchain.dao.PendingTxDao;
import bchain.domain.Hash;
import bchain.domain.Tx;
import bchain.util.ExtendedJdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static bchain.domain.Hash.hash;
import static java.util.stream.IntStream.of;

public class SqlitePendingTxDao implements PendingTxDao {
    @Autowired
    ExtendedJdbcTemplate jdbcTemplate;

    @Autowired
    SqliteTxDao txDao;

    @Override
    public void markPending(List<Hash> txs) {
        jdbcTemplate.batchUpdate("insert into PendingTx(txId) values (?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        long id = txDao.txId(txs.get(i));
                        ps.setLong(1, id);
                    }

                    @Override
                    public int getBatchSize() {
                        return txs.size();
                    }
                });
    }

    @Override
    public void unmarkPending(List<Hash> txs) {
        int[] nDeleted = jdbcTemplate.batchUpdate("delete from PendingTx where txId = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        long id = txDao.txId(txs.get(i));
                        ps.setLong(1, id);
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
    public List<Tx> allTx() {
        List<Optional<Long>> ids = jdbcTemplate.query("select txId from PendingTx",
                (rs, i) -> Optional.of(rs.getLong(1)));
        return txDao.allWithIds(ids);
    }

}
