package bchain.dao.sqlite;

import bchain.dao.BlockDao;
import bchain.dao.TxDao;
import bchain.domain.Block;
import bchain.domain.Hash;
import bchain.domain.Tx;
import bchain.domain.TxInput;
import bchain.util.ExtendedJdbcTemplate;
import bchain.util.PrepareMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static bchain.domain.Block.block;
import static bchain.domain.Hash.hash;
import static java.util.function.Function.identity;

public class SqliteBlockDao implements BlockDao {
    @Autowired
    ExtendedJdbcTemplate jdbcTemplate;

    @Autowired
    TxDao txDao;
    public static final PrepareMapper<Hash> HASH_PREPARE_MAPPER = (ps, n, hash) ->
            ps.setBytes(1, hash.getValues());

    @Override
    public boolean hasBlock(Hash hash) {
        Integer count = jdbcTemplate.queryForObject("select count(*) from Block where hash = ?",
                Integer.class,
                new Object[]{hash.getValues()});

        return count == 1;
    }

    @Override
    public void saveBlock(Block block) {
        for (Tx tx : block.getTxs()) {
            if (!txDao.hasTx(tx.getHash())) {
                txDao.saveTx(tx);
            }
        }

        jdbcTemplate.update("insert into Block(hash, prevBlockHash, nTxs) " +
                        "values(?, ?, ?)",
                block.getHash().getValues(),
                block.getPrevBlockHash().getValues(),
                block.getTxs().size());

        jdbcTemplate.batchUpdate("insert into BlockTx(hash, n, txHash) " +
                        "values(?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Tx tx = block.getTxs().get(i);
                        ps.setBytes(1, block.getHash().getValues());
                        ps.setInt(2, i);
                        ps.setBytes(3, tx.getHash().getValues());
                    }

                    @Override
                    public int getBatchSize() {
                        return block.getTxs().size();
                    }
                });
    }


    @Override
    public List<Block> all() {
        return allMatching("");
    }

    @Override
    public List<Block> allMatching(String criterion, Object... args) {
        List<Hash> blockHashes = jdbcTemplate.queryForList(
                "select hash from Block " + criterion, args, byte[].class)
                .stream()
                .map(Hash::hash)
                .collect(Collectors.toList());

        return allWith(blockHashes);
    }

    @Override
    public List<Block> allWith(List<Hash> hashes) {
        Map<Hash, Hash> prevBlockHashes = jdbcTemplate.queryMapSingleValue(
                "select prevBlockHash from Block " +
                        "where hash = ?",
                hashes,
                HASH_PREPARE_MAPPER,
                (rs, n) -> hash(rs.getBytes("prevBlockHash")));

        Map<Hash, List<Hash>> inputs = jdbcTemplate.queryMapList(
                "select txHash from BlockTx " +
                        "where hash = ? order by n",
                hashes,
                HASH_PREPARE_MAPPER,
                (rs, i) -> hash(rs.getBytes("txHash")));

        ArrayList<Hash> txHashesList = new ArrayList<>(inputs
                .values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet()));


        Map<Hash, Tx> txMap = txDao.allWith(txHashesList)
                .stream()
                .collect(Collectors.toMap(Tx::getHash, identity()));

        return hashes
                .stream()
                .map(hash -> block(
                        hash,
                        prevBlockHashes.get(hash),
                        inputs.get(hash)
                                .stream()
                                .map(txMap::get)
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Block> referencingBlocksByTx(Hash txHash) {
        List<Hash> hashes = jdbcTemplate.query(
                "select hash from BlockTx " +
                        "where txHash = ?",
                (rs, n) -> hash(rs.getBytes("hash")),
                new Object[] { txHash.getValues() });

        return allWith(hashes);
    }

    @Override
    public List<Block> referencingBlocksByBlock(Hash blockHash) {
        List<Hash> hashes = jdbcTemplate.query(
                "select hash from Block " +
                        "where prevBlockHash = ?",
                (rs, n) -> hash(rs.getBytes("hash")),
                new Object[] { blockHash.getValues() });

        return allWith(hashes);

    }
}
