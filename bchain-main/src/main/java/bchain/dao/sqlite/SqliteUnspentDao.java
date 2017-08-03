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



//    @Override
//    public void changeUnspent(PubKey address, long value) {
//        jdbcTemplate.update("insert or replace into Unspent(modulus, exponent, value) values (?, ?, " +
//                        "ifnull((select value from Unspent where modulus = ? and exponent = ?), 0) + ?" +
//                        ")",
//                ps -> {
//                    byte[] modulusBytes = address.getModulus().toByteArray();
//                    byte[] exponentBytes = address.getExponent().toByteArray();
//                    ps.setBytes(1, modulusBytes);
//                    ps.setBytes(2, exponentBytes);
//                    ps.setBytes(3, modulusBytes);
//                    ps.setBytes(4, exponentBytes);
//                    ps.setLong(5, value);
//                });
//    }

    @Override
    public long get(PubKey address) {
        return 0;
//        byte[] modulusBytes = address.getModulus().toByteArray();
//        byte[] exponentBytes = address.getExponent().toByteArray();
//
//        return jdbcTemplate.queryForObject("select ifnull(select value from Unspent where modulus = ? and exponent = ?, 0)",
//                Long.class, modulusBytes, exponentBytes);
    }

    @Override
    public void spendUnspend(List<UnspentTxOut> unspentTxOuts, List<UnspentTxOut> removeUnspentTxOuts) {


        jdbcTemplate.batchUpdate("insert into UnspentTxOut(hash, n) values (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        UnspentTxOut unspentTxOut = unspentTxOuts.get(i);
                        ps.setBytes(1, unspentTxOut.getHash().getValues());
                        ps.setInt(2, unspentTxOut.getN());
                    }

                    @Override
                    public int getBatchSize() {
                        return unspentTxOuts.size();
                    }
                });


        int[] nDeleted = jdbcTemplate.batchUpdate("delete from UnspentTxOut where hash = ? and n = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        UnspentTxOut unspentTxOut = removeUnspentTxOuts.get(i);
                        ps.setBytes(1, unspentTxOut.getHash().getValues());
                        ps.setInt(2, unspentTxOut.getN());
                    }

                    @Override
                    public int getBatchSize() {
                        return removeUnspentTxOuts.size();
                    }
                });

        if (!IntStream.of(nDeleted)
                .allMatch(n -> n == 1)) {
            throw new RuntimeException("Failed to remove unspent transaction out");
        }
    }
}
