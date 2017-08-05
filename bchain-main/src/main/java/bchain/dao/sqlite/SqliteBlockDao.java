package bchain.dao.sqlite;

import bchain.dao.BlockDao;
import bchain.domain.Block;
import bchain.domain.Hash;
import bchain.domain.Tx;
import bchain.util.ExtendedJdbcTemplate;
import bchain.util.PrepareMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static bchain.domain.Block.block;
import static bchain.domain.Hash.hash;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.function.Function.identity;

public class SqliteBlockDao implements BlockDao {
    @Autowired
    ExtendedJdbcTemplate jdbcTemplate;

    @Autowired
    SqliteTxDao txDao;

    public static final PrepareMapper<Hash> HASH_PREPARE_MAPPER = (ps, n, hash) ->
            ps.setBytes(1, hash.getValues());

    public static final PrepareMapper<Long> ID_PREPARE_MAPPER = (ps, n, id) ->
            ps.setLong(1, id);

    @Cacheable
    Optional<Long> blockIdOpt(Hash hash) {
        List<Long> res = jdbcTemplate.queryForList("select blockId from Block where hash = ?",
                Long.class, new Object[]{hash.getValues()});
        if (res.isEmpty()) {
            return empty();
        } else {
            return of(res.get(0));
        }
    }

    public Optional<Hash> blockHashOpt(long id) {
        List<byte[]> res = jdbcTemplate.queryForList("select hash from Block where blockId = ?",
                byte[].class, id);
        if (res.isEmpty()) {
            return empty();
        } else {
            return of(hash(res.get(0)));
        }
    }

    public Hash blockHash(long id) {
        return blockHashOpt(id)
                .orElseThrow(SqliteConfig.error("no block: " + id));
    }
    long blockId(Hash hash) {
        return blockIdOpt(hash)
                .orElseThrow(SqliteConfig.error("no block: " + hash));
    }

    @Override
    public boolean hasBlock(Hash hash) {
        return blockIdOpt(hash).isPresent();
    }


    @Override
    public void saveBlock(Block block) {
        Map<Hash, Long> txIds = new HashMap<>();
        for (Tx tx : block.getTxs()) {
            txDao.saveTx(tx);
            txIds.put(tx.getHash(),
                    txDao.txId(tx.getHash()));
        }

        jdbcTemplate.update("insert or ignore into Block(hash, prevBlockHash, nTxs) " +
                        "values(?, ?, ?)",
                block.getHash().getValues(),
                block.getPrevBlockHash() == null
                        ? null
                        : block.getPrevBlockHash().getValues(),
                block.getTxs().size());

        long blockId = blockId(block.getHash());

        jdbcTemplate.batchUpdate("insert or ignore into BlockTx(blockId, n, txId) " +
                        "values(?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Tx tx = block.getTxs().get(i);
                        ps.setLong(1, blockId);
                        ps.setInt(2, i);
                        ps.setLong(3, txIds.get(tx.getHash()));
                    }

                    @Override
                    public int getBatchSize() {
                        return block.getTxs().size();
                    }
                });
    }

    @Override
    public Block get(Hash from) {
        return allWith(singletonList(from)).get(0);
    }

    @Override
    public List<Block> all() {
        List<Optional<Long>> blockIds = jdbcTemplate.queryForList(
                "select blockId from Block ", Long.class)
                .stream()
                .map(Optional::of)
                .collect(Collectors.toList());

        return allWithIds(blockIds);
    }

    @Override
    public List<Block> allWith(List<Hash> hashes) {
        List<Optional<Long>> blockIds = hashes.stream()
                .map(this::blockIdOpt)
                .collect(Collectors.toList());

        return allWithIds(blockIds);
    }

    public List<Block> allWithIds(List<Optional<Long>> ids) {
        List<Long> presentIds = ids
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Map<Long, Hash> hashes = jdbcTemplate.queryMapSingleValue(
                "select hash from Block " +
                        "where blockId = ?",
                presentIds,
                ID_PREPARE_MAPPER,
                (rs, n) -> hash(rs.getBytes("hash")));


        Map<Long, Hash> prevBlockHashes = jdbcTemplate.queryMapSingleValue(
                "select prevBlockHash from Block " +
                        "where blockId = ?",
                presentIds,
                ID_PREPARE_MAPPER,
                (rs, n) -> hash(rs.getBytes("prevBlockHash")));

        Map<Long, List<Long>> txIds = jdbcTemplate.queryMapList(
                "select txId from BlockTx " +
                        "where blockId = ? order by n",
                presentIds,
                ID_PREPARE_MAPPER,
                (rs, i) -> rs.getLong("txId"));

        ArrayList<Optional<Long>> txIdsList = new ArrayList<>(txIds
                .values()
                .stream()
                .flatMap(List::stream)
                .map(Optional::of)
                .collect(Collectors.toSet()));

        Map<Long, Tx> txMap = txDao.allWithIds(txIdsList)
                .stream()
                .collect(Collectors.toMap(tx ->
                                txDao.txId(tx.getHash()),
                        identity()));

        return ids
                .stream()
                .map(id ->
                        id.map(idVal ->
                                block(
                                        hashes.get(idVal),
                                        prevBlockHashes.get(idVal),
                                        txIds.get(idVal)
                                                .stream()
                                                .map(txMap::get)
                                                .collect(Collectors.toList())))
                                .orElse(null))
                .collect(Collectors.toList());
    }

    @Override
    public List<Block> referencingBlocksByTx(Hash txHash) {
        List<Optional<Long>> blockIds = jdbcTemplate.query(
                "select blockId from BlockTx " +
                        "where txId = ?",
                (rs, n) -> of(rs.getLong("blockId")),
                txDao.txId(txHash));

        return allWithIds(blockIds);
    }

    @Override
    public List<Block> referencingBlocksByBlock(Hash blockHash) {
        List<Optional<Long>> blockIds = jdbcTemplate.query(
                "select blockId from Block " +
                        "where prevBlockHash = ?",
                (rs, n) -> of(rs.getLong("blockId")),
                new Object[]{blockHash.getValues()});

        return allWithIds(blockIds);

    }
}
