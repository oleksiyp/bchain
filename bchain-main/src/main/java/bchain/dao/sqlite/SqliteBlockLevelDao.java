package bchain.dao.sqlite;

import bchain.dao.BlockLevelDao;
import bchain.domain.Hash;
import bchain.util.ExtendedJdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

public class SqliteBlockLevelDao implements BlockLevelDao {
    @Autowired
    ExtendedJdbcTemplate jdbcTemplate;

    @Autowired
    SqliteBlockDao blockDao;

    @Override
    public int getLevel(Hash hash) {
        return jdbcTemplate.queryForObject("select ifnull((select level from BlockLevel where blockId = ?), 0) as level",
                (rs, i) -> rs.getInt("level"),
                blockDao.blockId(hash));
    }

    @Override
    public void setLevel(Hash hash, int level) {
        jdbcTemplate.update("insert or replace into BlockLevel(blockId, level) values (?, ?)",
                blockDao.blockId(hash), level);
    }
}
