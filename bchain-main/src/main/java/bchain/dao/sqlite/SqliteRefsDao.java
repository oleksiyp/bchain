package bchain.dao.sqlite;

import bchain.dao.RefsDao;
import bchain.domain.Hash;
import bchain.util.ExtendedJdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static bchain.domain.Hash.hash;

public class SqliteRefsDao implements RefsDao {
    public static final String HEAD_REF = "HEAD";
    public static final String MASTER_REF = "MASTER";

    @Autowired
    ExtendedJdbcTemplate jdbcTemplate;

    @Autowired
    SqliteBlockDao blockDao;

    @Override
    public Hash getHead() {
        return getRef(HEAD_REF);
    }

    @Override
    public void setHead(Hash newHead) {
        setRef(HEAD_REF, newHead);
    }

    @Override
    public Hash getMaster() {
        return getRef(MASTER_REF);
    }

    @Override
    public void setMaster(Hash newMaster) {
        setRef(MASTER_REF, newMaster);
    }

    private Hash getRef(String refName) {
        List<Hash> hashes = jdbcTemplate.query("select blockId from Refs where name = ?",
                (rs, i) -> blockDao.blockHash(rs.getLong(1)),
                refName);

        if (hashes.isEmpty()) {
            return null;
        } else if (hashes.size() > 1) {
            throw new RuntimeException("more than one ref for '" + refName + "'");
        } else {
            return hashes.get(0);
        }
    }

    private void setRef(String refName, Hash hash) {
        jdbcTemplate.update("insert or replace into Refs(blockId, name) values (?, ?)",
                blockDao.blockId(hash), refName);
    }
}
