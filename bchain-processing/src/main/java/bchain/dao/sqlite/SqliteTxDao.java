package bchain.dao.sqlite;

import bchain.dao.TxDao;
import bchain.domain.*;
import bchain.util.ExtendedJdbcTemplate;
import bchain.util.PrepareMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static bchain.domain.Hash.hash;
import static bchain.domain.PubKey.pubKey;
import static bchain.domain.Tx.tx;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class SqliteTxDao implements TxDao {
    public static final RowMapper<TxInput> TX_INPUT_ROW_MAPPER = (rs, rowNum) ->
            TxInput.input(
            hash(rs.getBytes("prevTxHash")),
            rs.getInt("outputIndex"),
            rs.getBytes("signature"));

    public static final RowMapper<TxOutput> TX_OUTPUT_ROW_MAPPER = (rs, rowNum) -> TxOutput.output(
            pubKey(rs.getBytes("modulus"),
                    rs.getBytes("exponent")),
            rs.getLong("value"));

    public static final PrepareMapper<Hash> HASH_PREPARE_MAPPER = (ps, n, hash) ->
            ps.setBytes(1, hash.getValues());

    public static final PrepareMapper<Long> ID_PREPARE_MAPPER = (ps, n, id) ->
            ps.setLong(1, id);


    @Autowired
    ExtendedJdbcTemplate jdbcTemplate;

    @Cacheable
    Optional<Long> txIdOpt(Hash hash) {
        List<Long> res = jdbcTemplate.queryForList("select txId from Tx where hash = ?",
                Long.class, new Object[]{hash.getValues()});
        if (res.isEmpty()) {
            return empty();
        } else {
            return of(res.get(0));
        }
    }

    long txId(Hash hash) {
        return txIdOpt(hash)
                .orElseThrow(SqliteConfig.error("no tx: " + hash));
    }

    @Cacheable
    Optional<Hash> txHashOpt(long id) {
        List<byte[]> res = jdbcTemplate.queryForList("select hash from Tx where txId = ?",
                byte[].class, id);
        if (res.isEmpty()) {
            return empty();
        } else {
            return of(hash(res.get(0)));
        }
    }

    Hash txHash(long id) {
        return txHashOpt(id)
                .orElseThrow(SqliteConfig.error("no tx: " + id));
    }

    @Cacheable
    Optional<Long> addressIdOpt(PubKey address) {
        List<Long> res = jdbcTemplate.queryForList("select addressId from Address where modulus = ? and exponent = ?",
                Long.class,
                address.getModulus().toByteArray(),
                address.getExponent().toByteArray());
        if (res.isEmpty()) {
            return empty();
        } else {
            return of(res.get(0));
        }

    }

    long addressId(PubKey address) {
        return addressIdOpt(address)
                .orElseThrow(SqliteConfig.error("no address: " + address));
    }

    @Override
    public boolean hasTx(Hash hash) {
        return txIdOpt(hash).isPresent();
    }

    @Override
    public boolean hasAll(Set<Hash> hashes) {
        return hashes.stream()
                .map(this::txIdOpt)
                .allMatch(Optional::isPresent);
    }

    @Override
    public boolean saveTx(Tx tx) {
        int nAdded = jdbcTemplate.update("insert or ignore into Tx(hash, coinbase, nInputs, nOutputs) " +
                        "values(?, ?, ?, ?)",
                tx.getHash().getValues(),
                tx.isCoinbase(),
                tx.getInputs().size(),
                tx.getOutputs().size());

        if (nAdded == 0) {
            return false;
        }

        long txId = txId(tx.getHash());

        jdbcTemplate.batchUpdate("insert or ignore into TxInput(txId, n, prevTxHash, outputIndex, signature) " +
                        "values(?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        TxInput input = tx.getInputs().get(i);
                        ps.setLong(1, txId);
                        ps.setInt(2, i);
                        ps.setBytes(3, input.getPrevTxHash().getValues());
                        ps.setInt(4, input.getOutputIndex());
                        ps.setBytes(5, input.getSignature());
                    }

                    @Override
                    public int getBatchSize() {
                        return tx.getInputs().size();
                    }
                });

        jdbcTemplate.batchUpdate("insert or ignore into Address(modulus, exponent) " +
                        "values(?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        TxOutput output = tx.getOutputs().get(i);
                        ps.setBytes(1, output.getAddress().getModulus().toByteArray());
                        ps.setBytes(2, output.getAddress().getExponent().toByteArray());
                    }

                    @Override
                    public int getBatchSize() {
                        return tx.getOutputs().size();
                    }
                });

        jdbcTemplate.batchUpdate("insert or ignore into TxOutput(txId, n, addressId, value) " +
                        "values(?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        TxOutput output = tx.getOutputs().get(i);
                        ps.setLong(1, txId);
                        ps.setInt(2, i);
                        ps.setLong(
                                3,
                                addressId(output.getAddress()));
                        ps.setLong(4, output.getValue());
                    }

                    @Override
                    public int getBatchSize() {
                        return tx.getOutputs().size();
                    }
                });

        return true;
    }

    @Override
    public List<Tx> all() {
        List<Long> txIds = jdbcTemplate.queryForList("select txId from Tx", Long.class);
        return allWithIds(txIds.stream()
                .map(Optional::of)
                .collect(Collectors.toList()));
    }

    @Override
    public List<Tx> allWith(List<Hash> hashes) {
        return allWithIds(hashes
                .stream()
                .map(this::txIdOpt)
                .collect(Collectors.toList()));
    }

    public List<Tx> allWithIds(List<Optional<Long>> ids) {

        List<Long> presentIds = ids
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Map<Long, Hash> hashes = jdbcTemplate.queryMapSingleValue(
                "select hash from Tx " +
                        "where txId = ?",
                presentIds,
                ID_PREPARE_MAPPER,
                (rs, n) -> hash(rs.getBytes("hash")));

        Map<Long, Boolean> coinbases = jdbcTemplate.queryMapSingleValue(
                "select coinbase from Tx " +
                        "where txId = ?",
                presentIds,
                ID_PREPARE_MAPPER,
                (rs, n) -> rs.getBoolean("coinbase"));

        Map<Long, List<TxInput>> inputs = jdbcTemplate.queryMapList(
                "select * from TxInput " +
                        "where txId = ? order by n",
                presentIds,
                ID_PREPARE_MAPPER,
                TX_INPUT_ROW_MAPPER);

        Map<Long, List<TxOutput>> outputs = jdbcTemplate.queryMapList(
                "select * " +
                        "from TxOutput txOut join Address addr " +
                        "on txOut.addressId = addr.addressId " +
                        "where txId = ? order by n",
                presentIds,
                ID_PREPARE_MAPPER,
                TX_OUTPUT_ROW_MAPPER);


        return ids
                .stream()
                .map(id ->
                        id.map(idVal ->
                                tx(hashes.get(idVal),
                                        coinbases.get(idVal),
                                        inputs.get(idVal),
                                        outputs.get(idVal)))
                                .orElse(null))
                .collect(Collectors.toList());
    }

    @Override
    public List<Tx> referencingTxs(Hash txHash) {
        List<Long> ids = jdbcTemplate.query(
                "select txId from TxInput " +
                        "where prevTxHash = ?",
                (rs, n) -> rs.getLong("txId"),
                new Object[]{txHash.getValues()});

        return allWithIds(ids.stream()
                .map(Optional::of)
                .collect(Collectors.toList()));
    }

}
