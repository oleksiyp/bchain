package bchain.dao.sqlite;

import bchain.dao.OrphanedTxDao;
import bchain.dao.TxDao;
import bchain.domain.Hash;
import bchain.util.ExtendedJdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static bchain.domain.Hash.hash;

public class SqliteOrphanedTxDao implements OrphanedTxDao {
    @Autowired
    ExtendedJdbcTemplate jdbcTemplate;

    @Autowired
    SqliteTxDao txDao;

    @Override
    public Set<Hash> all() {
        return new HashSet<>(jdbcTemplate.query("select txId from OrphanedTx",
                (rs, i) -> txDao.txHash(rs.getLong("txId"))));
    }

    @Override
    public boolean isOrphaned(Hash hash) {
        return jdbcTemplate.queryForObject("select count(*) as cnt from OrphanedTx " +
                        "where txId = ?",
                Integer.class,
                txDao.txId(hash)) == 1;
    }

    @Override
    public boolean isOrphanedAny(Set<Hash> hashes) {
        return jdbcTemplate.queryMapSingleValue(
                "select count(*) as cnt from OrphanedTx " +
                        "where txId = ?",
                hashes.stream()
                        .map(txDao::txId)
                        .collect(Collectors.toList()),
                SqliteTxDao.ID_PREPARE_MAPPER,
                (rs, n) -> rs.getInt("cnt"))
                .values()
                .stream()
                .anyMatch(x -> x == 1);
    }

    @Override
    public void add(Hash hash) {
        jdbcTemplate.update("insert or ignore into OrphanedTx(txId) values(?)",
                txDao.txId(hash));

    }

    @Override
    public void remove(Hash hash) {
        jdbcTemplate.update("delete from OrphanedTx where txId = ?",
                txDao.txId(hash));
    }
}
