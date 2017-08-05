package bchain.dao.sqlite;

import bchain.domain.Block;
import bchain.domain.Hash;
import bchain.domain.Tx;

import static bchain.domain.Hash.hashOf;
import static bchain.domain.PubKey.pubKey;
import static bchain.domain.TxInput.input;
import static bchain.domain.TxOutput.output;
import static bchain.util.RndUtil.rndBytes;

public class TestObjects {
    public static final Tx TEST_TX1 = Tx.builder()
            .add(input(hashOf("abc"), 0, rndBytes(16)))
            .add(input(hashOf("def"), 1, rndBytes(16)))
            .add(input(hashOf("ghi"), 2, rndBytes(16)))
            .add(output(pubKey(rndBytes(64), rndBytes(64)), 2000))
            .build();

    public static final Tx TEST_TX2 = Tx.builder()
            .add(input(hashOf("abc"), 0, rndBytes(16)))
            .add(input(hashOf("def"), 1, rndBytes(16)))
            .add(input(hashOf("ghi"), 2, rndBytes(16)))
            .add(output(pubKey(rndBytes(64), rndBytes(64)), 2000))
            .build();

    public static final Block TEST_BLOCK1 = testBlock(hashOf("abc"));

    public static final Block TEST_BLOCK2 = testBlock(TEST_BLOCK1.getHash());

    public static Block testBlock(Hash prevBlockHash) {
        return Block.builder()
                .setPrevBlockHash(prevBlockHash)
                .add(Tx.builder()
                        .add(input(hashOf("abc"), 0, rndBytes(16)))
                        .add(input(hashOf("def"), 1, rndBytes(16)))
                        .add(input(hashOf("ghi"), 2, rndBytes(16)))
                        .add(output(pubKey(rndBytes(64), rndBytes(64)), 2000))
                        .build())
                .add(Tx.builder()
                        .add(input(hashOf("jkl"), 0, rndBytes(16)))
                        .add(input(hashOf("mno"), 1, rndBytes(16)))
                        .add(input(hashOf("prs"), 2, rndBytes(16)))
                        .add(output(pubKey(rndBytes(64), rndBytes(64)), 2000))
                        .build())
                .build();
    }
}
