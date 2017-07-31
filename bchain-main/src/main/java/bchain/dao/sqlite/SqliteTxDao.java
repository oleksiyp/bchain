package bchain.dao.sqlite;

import bchain.dao.TxDao;
import bchain.domain.Hash;
import bchain.domain.Tx;
import bchain.domain.TxInput;
import bchain.domain.TxOutput;
import bchain.util.ExtendedJdbcTemplate;
import bchain.util.PrepareMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static bchain.domain.Hash.hash;
import static bchain.domain.PubKey.pubKey;
import static bchain.domain.Tx.tx;

public class SqliteTxDao implements TxDao {
    public static final RowMapper<TxInput> TX_INPUT_ROW_MAPPER = (rs, rowNum) -> TxInput.input(
            hash(rs.getBytes("prevTxHash")),
            rs.getInt("outputIndex"),
            rs.getBytes("signature"));

    public static final RowMapper<TxOutput> TX_OUTPUT_ROW_MAPPER = (rs, rowNum) -> TxOutput.output(
            pubKey(rs.getBytes("modulus"),
                    rs.getBytes("exponent")),
            rs.getLong("value"));

    public static final PrepareMapper<Hash> HASH_PREPARE_MAPPER = (ps, n, hash) ->
            ps.setBytes(1, hash.getValues());


    @Autowired
    ExtendedJdbcTemplate jdbcTemplate;

    @Override
    public boolean hasTx(Hash hash) {
        Integer count = jdbcTemplate.queryForObject("select count(*) from Tx where hash = ?",
                Integer.class,
                new Object[]{hash.getValues()});

        return count == 1;
    }

    @Override
    public boolean hasAll(Set<Hash> hashes) {
        return jdbcTemplate.queryMapSingleValue(
                "select count(*) as cnt from Tx " +
                        "where hash = ?",
                new ArrayList<>(hashes),
                HASH_PREPARE_MAPPER,
                (rs, n) -> rs.getInt("cnt"))
                .values()
                .stream()
                .allMatch(x -> x == 1);
    }

    @Override
    public void saveTx(Tx tx) {
        jdbcTemplate.update("insert into Tx(hash, coinbase, nInputs, nOutputs) " +
                        "values(?, ?, ?, ?)",
                tx.getHash().getValues(),
                tx.isCoinbase(),
                tx.getInputs().size(),
                tx.getOutputs().size());

        jdbcTemplate.batchUpdate("insert into TxInput(hash, n, prevTxHash, outputIndex, signature) " +
                        "values(?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        TxInput input = tx.getInputs().get(i);
                        ps.setBytes(1, tx.getHash().getValues());
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

        jdbcTemplate.batchUpdate("insert into TxOutput(hash, n, modulus, exponent, value) " +
                        "values(?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        TxOutput output = tx.getOutputs().get(i);
                        ps.setBytes(1, tx.getHash().getValues());
                        ps.setInt(2, i);
                        ps.setBytes(3, output.getAddress().getModulus().toByteArray());
                        ps.setBytes(4, output.getAddress().getExponent().toByteArray());
                        ps.setLong(5, output.getValue());
                    }

                    @Override
                    public int getBatchSize() {
                        return tx.getOutputs().size();
                    }
                });
    }

    @Override
    public List<Tx> all() {
        return allMatching("");
    }

    @Override
    public List<Tx> allMatching(String criterion, Object... args) {
        List<Hash> txHashes = jdbcTemplate.queryForList(
                "select hash from Tx " + criterion, args, byte[].class)
                .stream()
                .map(Hash::hash)
                .collect(Collectors.toList());

        return allWith(txHashes);
    }

    @Override
    public List<Tx> allWith(List<Hash> hashes) {
        Map<Hash, Boolean> coinbases = jdbcTemplate.queryMapSingleValue(
                "select coinbase from Tx " +
                        "where hash = ?",
                hashes,
                HASH_PREPARE_MAPPER,
                (rs, n) -> rs.getBoolean("coinbase"));

        Map<Hash, List<TxInput>> inputs = jdbcTemplate.queryMapList(
                "select * from TxInput " +
                        "where hash = ? order by n",
                hashes,
                HASH_PREPARE_MAPPER,
                TX_INPUT_ROW_MAPPER);

        Map<Hash, List<TxOutput>> outputs = jdbcTemplate.queryMapList(
                "select * from TxOutput " +
                        "where hash = ? order by n",
                hashes,
                HASH_PREPARE_MAPPER,
                TX_OUTPUT_ROW_MAPPER);


        return hashes.stream()
                .map(hash -> tx(hash,
                        coinbases.get(hash),
                        inputs.get(hash),
                        outputs.get(hash)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Tx> referencingTxs(Hash txHash) {
        List<Hash> hashes = jdbcTemplate.query(
                "select hash from TxInput " +
                        "where prevTxHash = ?",
                (rs, n) -> hash(rs.getBytes("hash")),
                new Object[] { txHash.getValues() });

        return allWith(hashes);
    }

}
