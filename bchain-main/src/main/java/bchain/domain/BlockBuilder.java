package bchain.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class BlockBuilder {
    @Getter
    @Setter
    private Hash prevBlockHash;

    @Getter
    private List<Tx> txs;

    public BlockBuilder() {
        txs = new ArrayList<>();
    }

    public BlockBuilder add(Tx tx) {
        txs.add(tx);
        return this;
    }

    public Block build() {
        Hash hash = BlockHash.computeHash(prevBlockHash, txs);
        return Block.block(hash, prevBlockHash, txs);
    }
}
