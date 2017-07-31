package bchain.dao.sqlite;

import bchain.dao.OrphanedBlockDao;
import bchain.domain.Hash;
import bchain.util.ExtendedJdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

import static bchain.domain.Hash.hash;

public class SqliteOrphanedBlockDao implements OrphanedBlockDao {
    @Autowired
    ExtendedJdbcTemplate jdbcTemplate;

    @Override
    public boolean isOrphaned(Hash hash) {
        return jdbcTemplate.queryForObject("select count(*) as cnt from OrphanedBlock " +
                        "where hash = ?",
                Integer.class,
                new Object[] { hash.getValues() }) == 1;
    }

    @Override
    public void add(Hash hash) {
        jdbcTemplate.update("insert into OrphanedBlock(hash) values(?)",
                new Object[] { hash.getValues() });

    }

    @Override
    public void remove(Hash hash) {
        jdbcTemplate.update("delete from OrphanedBlock where hash = ?",
                new Object[] { hash.getValues() });
    }

    @Override
    public Set<Hash> all() {
        return new HashSet<>(jdbcTemplate.query("select hash from OrphanedBlock",
                (rs, i) -> hash(rs.getBytes("hash"))));
    }
}
