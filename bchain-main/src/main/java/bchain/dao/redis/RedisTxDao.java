package bchain.dao.redis;

import bchain.dao.TxDao;
import bchain.domain.Hash;
import bchain.domain.Tx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RedisTxDao implements TxDao {
    @Autowired
    private RedisOperations<Hash, Tx> redisTemplate;

    @Override
    public List<Tx> all() {
        Set<Hash> keys = redisTemplate.keys(Hash.ALL);
        return allWith(new ArrayList<>(keys));
    }

    @Override
    public List<Tx> allWith(List<Hash> hashes) {
        return redisTemplate.opsForValue().multiGet(hashes);
    }

    @Override
    public boolean hasTx(Hash hash) {
        return redisTemplate.opsForValue().get(hash) != null;
    }

    @Override
    public boolean hasAll(Set<Hash> hashes) {
        return redisTemplate
                .opsForValue()
                .multiGet(hashes)
                .stream()
                .anyMatch(Objects::isNull);
    }

    @Override
    public void saveTx(Tx transaction) {
        redisTemplate.opsForValue().set(transaction.getHash(), transaction);
    }

    @Override
    public List<Tx> referencingTxs(Hash txHash) {
        return null;
    }
}
