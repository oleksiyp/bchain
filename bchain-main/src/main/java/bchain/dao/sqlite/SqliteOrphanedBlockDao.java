package bchain.dao.sqlite;

import bchain.dao.OrphanedBlockDao;
import bchain.domain.Hash;
import bchain.util.ExtendedJdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

public class SqliteOrphanedBlockDao implements OrphanedBlockDao {
    @Autowired
    ExtendedJdbcTemplate jdbcTemplate;

    @Autowired
    SqliteBlockDao blockDao;

    @Override
    public boolean isOrphaned(Hash hash) {
        return jdbcTemplate.queryForObject("select count(*) as cnt from OrphanedBlock " +
                        "where blockId = ?",
                Integer.class,
                blockDao.blockId(hash)) == 1;
    }

    @Override
    public void add(Hash hash) {
        jdbcTemplate.update("insert or ignore into OrphanedBlock(blockId) values(?)",
                blockDao.blockId(hash));

    }


    @Override
    public void remove(Hash hash) {
        jdbcTemplate.update("delete from OrphanedBlock where blockId = ?",
                blockDao.blockId(hash));
    }
}
