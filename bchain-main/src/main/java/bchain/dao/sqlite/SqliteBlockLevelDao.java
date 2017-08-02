package bchain.dao.sqlite;

import bchain.dao.BlockLevelDao;
import bchain.domain.Hash;
import bchain.util.ExtendedJdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

public class SqliteBlockLevelDao implements BlockLevelDao {
    @Autowired
    ExtendedJdbcTemplate jdbcTemplate;

    @Override
    public int getLevel(Hash hash) {
        return jdbcTemplate.queryForObject("select ifnull((select level from BlockLevel where hash = ?), 0) as level",
                (rs, i) -> rs.getInt("level"),
                new Object[] { hash.getValues() });
    }

    @Override
    public void setLevel(Hash hash, int level) {
        jdbcTemplate.update("insert or replace into BlockLevel(hash, level) values (?, ?)",
                hash.getValues(), level);
    }
}
