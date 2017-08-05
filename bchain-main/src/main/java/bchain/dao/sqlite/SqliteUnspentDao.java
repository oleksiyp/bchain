package bchain.dao.sqlite;

import bchain.dao.UnspentDao;
import bchain.domain.Hash;
import bchain.domain.PubKey;
import bchain.domain.UnspentTxOut;
import bchain.util.ExtendedJdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;


public class SqliteUnspentDao implements UnspentDao {
    @Autowired
    ExtendedJdbcTemplate jdbcTemplate;

    @Autowired
    SqliteTxDao txDao;



//    @Override
//    public void changeUnspent(PubKey address, long value) {
//    }

    @Override
    public long get(PubKey address) {
        return jdbcTemplate.queryForObject(
                "select ifnull(select value from Unspent where addressId = ?, 0)",
                Long.class, txDao.addressId(address));
    }

    @Override
    public void spendUnspend(List<UnspentTxOut> add,
                             List<UnspentTxOut> remove) {

        jdbcTemplate.batchUpdate("insert into UnspentTxOut(txId, n) values (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        UnspentTxOut unspentTxOut = add.get(i);
                        ps.setLong(1, txDao.txId(unspentTxOut.getHash()));
                        ps.setInt(2, unspentTxOut.getN());
                    }

                    @Override
                    public int getBatchSize() {
                        return add.size();
                    }
                });


        int[] nDeleted = jdbcTemplate.batchUpdate("delete from UnspentTxOut where txId = ? and n = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        UnspentTxOut unspentTxOut = remove.get(i);
                        ps.setLong(1, txDao.txId(unspentTxOut.getHash()));
                        ps.setInt(2, unspentTxOut.getN());
                    }

                    @Override
                    public int getBatchSize() {
                        return remove.size();
                    }
                });

        if (!IntStream.of(nDeleted)
                .allMatch(n -> n == 1)) {
            throw new RuntimeException("Failed to remove unspent transaction out");
        }

        changeUnsent(add, 1);
        changeUnsent(remove, -1);
    }

    private void changeUnsent(List<UnspentTxOut> add, long mult) {
        jdbcTemplate.batchUpdate(
                "insert or replace into Unspent(addressId, value) values (?, " +
                        "ifnull((select value from Unspent where addressId = ?), 0) + ?" +
                        ")",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {

                        UnspentTxOut txOut = add.get(i);
                        long addressId = txDao.addressId(txOut.getAddress());
                        ps.setLong(1, addressId);
                        ps.setLong(2, addressId);
                        ps.setLong(3, txOut.getValue() * mult);
                    }

                    @Override
                    public int getBatchSize() {
                        return add.size();
                    }
                });
    }
}
