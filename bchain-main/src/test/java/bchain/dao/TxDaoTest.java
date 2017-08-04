package bchain.dao;


import bchain.domain.Tx;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static bchain.domain.Hash.hashOf;
import static bchain.domain.PubKey.pubKey;
import static bchain.domain.TxInput.input;
import static bchain.domain.TxOutput.output;
import static bchain.util.RndUtil.rndBytes;
import static org.assertj.core.api.Assertions.assertThat;

public class TxDaoTest {

    @Autowired
    TxDao txDao;

    @Test
    @FlywayTest
    public void saveTx() {
        // given
        Tx tx = Tx.builder()
                .add(input(hashOf("abc"), 0, rndBytes(16)))
                .add(input(hashOf("def"), 1, rndBytes(16)))
                .add(input(hashOf("ghi"), 2, rndBytes(16)))
                .add(output(pubKey(rndBytes(64), rndBytes(64)), 2000))
                .build();

        // when
        txDao.saveTx(tx);

        // then
        assertThat(txDao.all())
                .hasSize(1)
                .contains(tx);
    }

    @Test
    @FlywayTest
    public void hasTx() {
        // given
        Tx tx = Tx.builder()
                .add(input(hashOf("abc"), 0, rndBytes(16)))
                .add(input(hashOf("def"), 1, rndBytes(16)))
                .add(input(hashOf("ghi"), 2, rndBytes(16)))
                .add(output(pubKey(rndBytes(64), rndBytes(64)), 2000))
                .build();

        // when
        boolean before = txDao.hasTx(tx.getHash());
        txDao.saveTx(tx);
        boolean after = txDao.hasTx(tx.getHash());

        // then
        assertThat(before).isEqualTo(false);
        assertThat(after).isEqualTo(true);
    }

}