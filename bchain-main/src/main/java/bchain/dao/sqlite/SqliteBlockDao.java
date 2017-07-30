package bchain.dao.sqlite;

import bchain.dao.BlockDao;
import bchain.domain.Block;
import bchain.domain.Hash;

public class SqliteBlockDao implements BlockDao {
    @Override
    public boolean hasBlock(Hash hash) {
        return false;
    }

    @Override
    public void saveBlock(Block block) {

    }
}
