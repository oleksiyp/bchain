package bchain.dao.sqlite;

import bchain.dao.UnspentDao;
import bchain.domain.Hash;
import bchain.domain.PubKey;
import bchain.util.ExtendedJdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;


public class SqliteUnspentDao implements UnspentDao {
    @Autowired
    ExtendedJdbcTemplate jdbcTemplate;


    @Override
    public void addTxOut(Hash txHash, int index) {
        jdbcTemplate.update("insert into UnspentTxOut(hash, n) values (?, ?)",
                txHash.getValues(), index);
    }

    @Override
    public void removeTxOut(Hash txHash, int index) {
        int nDeleted = jdbcTemplate.update("delete from UnspentTxOut where hash = ? and n = ?",
                txHash.getValues(), index);
        if (nDeleted != 1) {
            throw new RuntimeException("Failed to remove unspent transaction out");
        }
    }

    @Override
    public void changeUnspent(PubKey address, long value) {
        jdbcTemplate.update("insert or replace into Unspent(modulus, exponent, value) values (?, ?, " +
                        "ifnull((select value from Unspent where modulus = ? and exponent = ?), 0) + ?" +
                        ")",
                ps -> {
                    byte[] modulusBytes = address.getModulus().toByteArray();
                    byte[] exponentBytes = address.getExponent().toByteArray();
                    ps.setBytes(1, modulusBytes);
                    ps.setBytes(2, exponentBytes);
                    ps.setBytes(3, modulusBytes);
                    ps.setBytes(4, exponentBytes);
                    ps.setLong(5, value);
                });
    }
}
