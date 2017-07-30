package bchain.dao.sqlite;

import bchain.domain.Tx;
import bchain.domain.Hash;
import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.Random;

import static bchain.domain.Hash.hashOf;
import static bchain.domain.PubKey.pubKey;
import static bchain.domain.TxInput.input;
import static bchain.domain.TxOutput.output;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SqliteTestConfig.class, JdbcTemplateAutoConfiguration.class})
@TestExecutionListeners(value = {DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class})
public class SqliteTxDaoTest {

    @Autowired
    SqliteTxDao txDao;

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

    private byte[] rndBytes(int n) {
        byte[] bytes = new byte[n];
        Random rnd = new Random();
        rnd.nextBytes(bytes);
        return bytes;
    }
}