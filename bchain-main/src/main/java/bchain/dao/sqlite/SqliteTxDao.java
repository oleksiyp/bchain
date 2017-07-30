package bchain.dao.sqlite;

import bchain.dao.TxDao;
import bchain.domain.Hash;
import bchain.domain.Tx;
import bchain.domain.TxInput;
import bchain.domain.TxOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static bchain.domain.Hash.hash;
import static bchain.domain.PubKey.pubKey;
import static bchain.domain.Tx.tx;

public class SqliteTxDao implements TxDao {
    public static final RowMapper<TxInput> INPUT_ROW_MAPPER = (rs, rowNum) -> TxInput.input(
            hash(rs.getBytes("prevTxHash")),
            rs.getInt("outputIndex"),
            rs.getBytes("signature"));

    public static final RowMapper<TxOutput> OUTPUT_ROW_MAPPER = (rs, rowNum) -> TxOutput.output(
            pubKey(rs.getBytes("modulus"),
                    rs.getBytes("exponent")),
            rs.getLong("value"));

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public Tx findTx(Hash hash) {
        List<Tx> res = allMatching("where hash = ?", new Object[] { hash.getValues() });
        if (res.isEmpty()) {
            return null;
        } else {
            return res.get(0);
        }
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

    public List<Tx> all() {
        return allMatching("");
    }

    private List<Tx> allMatching(String criterion, Object... args) {
        List<Hash> txHashes = jdbcTemplate.queryForList(
                "select hash from Tx " + criterion, args, byte[].class)
                .stream()
                .map(Hash::hash)
                .collect(Collectors.toList());

        PrepareMapper<Hash> hashInMapper = (ps, n, hash) ->
                ps.setBytes(1, hash.getValues());

        Map<Hash, Boolean> coinbases = queryMapSingleValue(
                "select coinbase from Tx " +
                        "where hash = ?",
                    txHashes,
                hashInMapper,
                (rs, n) -> rs.getBoolean("coinbase"));

        Map<Hash, List<TxInput>> inputs = queryMapList(
                "select * from TxInput " +
                        "where hash = ? order by n",
                txHashes,
                hashInMapper,
                INPUT_ROW_MAPPER);

        Map<Hash, List<TxOutput>> outputs = queryMapList(
                "select * from TxOutput " +
                        "where hash = ? order by n",
                txHashes,
                hashInMapper,
                OUTPUT_ROW_MAPPER);


        return txHashes.stream()
                .map(hash -> tx(hash,
                        coinbases.get(hash),
                        inputs.get(hash),
                        outputs.get(hash)))
                .collect(Collectors.toList());
    }

    private <K, V> Map<K, V> queryMapSingleValue(String sql,
                                                 List<K> params,
                                                 PrepareMapper<K> prepareMapper,
                                                 RowMapper<V> rowMapper) {
        return jdbcTemplate.execute(sql, (PreparedStatementCallback<Map<K, V>>) ps -> {
            Map<K, V> map = new HashMap<>();
            int n = 0;
            for (K param : params) {
                prepareMapper.prepare(ps, n++, param);
                ResultSet rs = ps.executeQuery();
                int rowNum = 0;
                while (rs.next()) {
                    map.put(param, rowMapper.mapRow(rs, ++rowNum));
                }
            }
            return map;
        });
    }


    private <K, V> Map<K, List<V>> queryMapList(String sql,
                                                List<K> params,
                                                PrepareMapper<K> keyMapper,
                                                RowMapper<V> rowMapper) {
        return jdbcTemplate.execute(sql, (PreparedStatementCallback<Map<K, List<V>>>) ps -> {
            Map<K, List<V>> map = new HashMap<>();
            int n = 0;
            for (K param : params) {
                keyMapper.prepare(ps, n++, param);
                ResultSet rs = ps.executeQuery();
                List<V> lst = new ArrayList<>();
                int rowNum = 0;
                while (rs.next()) {
                    lst.add(rowMapper.mapRow(rs, ++rowNum));
                }
                map.put(param, lst);
            }
            return map;
        });
    }

    interface PrepareMapper<T> {
        void prepare(PreparedStatement ps, int n, T value) throws SQLException;
    }

//    private static class TxHashParamSetter implements BatchPreparedStatementSetter {
//        private final List<Hash> txList;
//
//        public TxHashParamSetter(List<Hash> txList) {
//            this.txList = txList;
//        }
//
//        @Override
//        public void setValues(PreparedStatement ps, int i) throws SQLException {
//            ps.setBytes(0, txList.get(i).getValues());
//        }
//
//        @Override
//        public int getBatchSize() {
//            return txList.size();
//        }
//    }
}
