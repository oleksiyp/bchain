package bchain.dao.sqlite;

import bchain.dao.OrphanedTxDao;
import bchain.domain.Hash;
import bchain.util.ExtendedJdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static bchain.domain.Hash.hash;

public class SqliteOrphanedTxDao implements OrphanedTxDao {
    @Autowired
    ExtendedJdbcTemplate jdbcTemplate;

    @Override
    public Set<Hash> all() {
        return new HashSet<>(jdbcTemplate.query("select hash from OrphanedTx",
                (rs, i) -> hash(rs.getBytes("hash"))));
    }

    @Override
    public boolean isOrphaned(Hash hash) {
        return jdbcTemplate.queryForObject("select count(*) as cnt from OrphanedTx " +
                "where hash = ?",
                Integer.class,
                new Object[] { hash.getValues() }) == 1;
    }

    @Override
    public boolean isOrphanedAny(Set<Hash> hashes) {
        return jdbcTemplate.queryMapSingleValue(
                "select count(*) as cnt from OrphanedTx " +
                        "where hash = ?",
                new ArrayList<>(hashes),
                SqliteTxDao.HASH_PREPARE_MAPPER,
                (rs, n) -> rs.getInt("cnt"))
                .values()
                .stream()
                .anyMatch(x -> x == 1);
    }

    @Override
    public void add(Hash hash) {
        jdbcTemplate.update("insert into OrphanedTx(hash) values(?)",
                new Object[] { hash.getValues() });

    }

    @Override
    public void remove(Hash hash) {
        jdbcTemplate.update("delete from OrphanedTx where hash = ?",
                new Object[] { hash.getValues() });
    }
}
