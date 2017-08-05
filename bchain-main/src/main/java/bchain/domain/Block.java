package bchain.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

import static bchain.domain.Hash.hashOf;
import static com.google.common.collect.ImmutableList.copyOf;

@Getter
@ToString
@EqualsAndHashCode
public class Block {
    public static final Hash GENESIS_HASH = hashOf("abc");

    private final Hash hash;
    private final Hash prevBlockHash;
    private final List<Tx> txs;

    private Block(Hash hash, Hash prevBlockHash, List<Tx> txs) {
        this.hash = hash;
        this.prevBlockHash = prevBlockHash;
        this.txs = copyOf(txs);
    }

    public static Block block(Hash hash, Hash prevBlockHash, List<Tx> txs) {
        return new Block(hash, prevBlockHash, txs);
    }

    public static BlockBuilder builder() {
        return new BlockBuilder();
    }

    public boolean isGenesis() {
        return prevBlockHash == null;
    }
}
