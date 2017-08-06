package bchain.dao.sqlite;

import bchain.dao.UnspentDao;
import bchain.domain.Hash;
import bchain.domain.PubKey;
import bchain.domain.TxOutput;
import bchain.domain.UnspentTxOut;
import bchain.util.ExtendedJdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static bchain.domain.PubKey.pubKey;
import static java.util.stream.IntStream.of;


public class SqliteUnspentDao implements UnspentDao {
    @Autowired
    ExtendedJdbcTemplate jdbcTemplate;

    @Autowired
    SqliteTxDao txDao;



//    @Override
//    public void changeUnspent(PubKey address, long value) {
//    }

    @Override
    public long unspentAmount(PubKey address) {
        return jdbcTemplate.queryForObject(
                "select ifnull(select value from Unspent where addressId = ?, 0)",
                Long.class, txDao.addressId(address));
    }

    @Override
    public void spendUnspend(List<UnspentTxOut> add,
                             List<UnspentTxOut> remove) {

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

        if (!of(nDeleted).allMatch(n -> n == 1)) {
            throw new RuntimeException("Failed to remove unspent transaction out");
        }

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




        changeUnsent(add, 1);
        changeUnsent(remove, -1);
    }

    @Override
    public UnspentTxOut get(Hash hash, int outputIndex) {
        List<UnspentTxOut> output = jdbcTemplate.query(
                "select addr.modulus, addr.exponent, txOut.value " +
                        "from UnspentTxOut unspentTxOut " +
                        "join TxOutput txOut " +
                        "on unspentTxOut.txId = txOut.txId " +
                        "and unspentTxOut.n = txOut.n " +
                        "join Address addr " +
                        "on txOut.addressId = addr.addressId " +
                        "where txOut.txId = ? and txOut.n = ?",
                (rs, rowNum) -> new UnspentTxOut(
                        hash,
                        outputIndex,
                        pubKey(rs.getBytes("modulus"),
                                rs.getBytes("exponent")),
                        rs.getLong("value")),
                txDao.txId(hash), outputIndex);

        if (output.isEmpty()) {
            return null;
        } else {
            return output.get(0);
        }
    }

    @Override
    public List<UnspentTxOut> unspents(PubKey address) {
        Optional<Long> addressIdOpt = txDao.addressIdOpt(address);
        if (!addressIdOpt.isPresent()) {
            return new ArrayList<>();
        }
        long addressId = addressIdOpt.get();

        return jdbcTemplate.query(
                "select txOut.txId, txOut.n, txOut.value " +
                        "from UnspentTxOut unspentTxOut " +
                        "join TxOutput txOut " +
                        "on unspentTxOut.txId = txOut.txId " +
                        "and unspentTxOut.n = txOut.n " +
                        "where txOut.addressId = ? " +
                        "and not exists (" +
                        "select pendTx.txId " +
                        "from PendingTx pendTx " +
                        "join TxInput txIn " +
                        "on pendTx.txId = txIn.txId " +
                        "where txIn.prevTxHash = (select hash from Tx where txId = txOut.txId) " +
                        "and txIn.outputIndex = txOut.n" +
                        ")",
                (rs, rowNum) -> new UnspentTxOut(
                        txDao.txHash(rs.getLong("txId")),
                        rs.getInt("n"),
                        address,
                        rs.getLong("value")),
                addressId);
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
