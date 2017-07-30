package bchain_mini;

import org.junit.Before;
import org.junit.Test;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.*;

public class BlockChainTest {
    private KeyPairGenerator rsaKeyGen;

    @Before
    public void setUp() throws Exception {
        rsaKeyGen = KeyPairGenerator.getInstance("RSA");
        rsaKeyGen.initialize(512);
    }

    @Test
    public void test2() throws InvalidKeyException, NoSuchAlgorithmException {
        Block block1 = new Block(null, genKey().getPublic());
        Transaction tx1 = new Transaction(10, genKey().getPublic());
        block1.addTransaction(tx1);
        block1.finalize();

        BlockChain bc = new BlockChain(block1);

        Block block2 = new Block(block1.getHash(), genKey().getPublic());
        Transaction tx2 = new Transaction();
        tx2.addInput(tx1.getHash(), 0);
        tx2.addOutput(10, genKey().getPublic());
        tx2.finalize();
        block2.addTransaction(tx2);
        block2.finalize();

        assertTrue(bc.addBlock(block2));
    }

    private KeyPair genKey() {
        return rsaKeyGen.genKeyPair();
    }
}