package bchain.domain;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class BlockBuilder {
    @Getter
    private Hash prevBlockHash;

    @Getter
    private byte []nounce;

    @Getter
    private List<Tx> txs;

    public BlockBuilder() {
        nounce = new byte[16];
        txs = new ArrayList<>();
    }

    public BlockBuilder setPrevBlockHash(Hash prevBlockHash) {
        this.prevBlockHash = prevBlockHash;
        return this;
    }

    public BlockBuilder setNounce(byte[] nounce) {
        this.nounce = nounce;
        return this;
    }

    public BlockBuilder add(Tx tx) {
        txs.add(tx);
        return this;
    }

    public Block build() {
        Hash hash = Crypto.computeBlockHash(prevBlockHash, nounce, txs);
        return Block.block(hash, prevBlockHash, nounce, txs);
    }
}
