package bchain.app;

import bchain.app.result.Result;
import bchain.dao.OrphanedBlockDao;
import bchain.dao.OrphanedTxDao;
import bchain.dao.TxDao;
import bchain.dao.sqlite.SqliteTestConfig;
import bchain.domain.Block;
import bchain.domain.Tx;
import bchain.util.RndUtil;
import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.ArrayList;
import java.util.List;

import static bchain.dao.sqlite.TestObjects.rndPrivKey;
import static bchain.domain.TxInput.input;
import static bchain.domain.TxOutput.output;
import static bchain.util.RndUtil.rndBytes;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SqliteTestConfig.class, JdbcTemplateAutoConfiguration.class, OrphanedProcessorTest.class})
@TestExecutionListeners(value = {DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class})
public class OrphanedProcessorTest {

    @Autowired
    TxDao txDao;

    @Autowired
    OrphanedProcessor processor;

    @Autowired
    OrphanedTxDao orphanedTxDao;

    @Autowired
    OrphanedBlockDao orphanedBlockDao;

    List<Tx> deOrphanedTxs;
    List<Block> deOrphanedBlocks;

    @Before
    public void setUp() throws Exception {
        deOrphanedTxs = new ArrayList<>();
        deOrphanedBlocks = new ArrayList<>();
    }

    @Bean
    public OrphanedProcessor orphanedProcessor() {
        return new OrphanedProcessor();
    }

    @Test
    @FlywayTest
    public void processTx() throws Exception {
        // given
        Tx firstTx = Tx.builder()
                .setCoinbase(true)
                .add(output(RndUtil.rndPubKey(), 2000))
                .build();

        Tx secondTx = Tx.builder()
                .setCoinbase(false)
                .add(input(firstTx.getHash(), 0, rndBytes(32)), rndPrivKey())
                .add(output(RndUtil.rndPubKey(), 2000))
                .build();

        Tx thirdTx = Tx.builder()
                .setCoinbase(false)
                .add(input(secondTx.getHash(), 0, rndBytes(32)), rndPrivKey())
                .build();

        // when

        txDao.saveTx(secondTx);
        Result res1 = processor.process(secondTx);
        txDao.saveTx(thirdTx);
        Result res2 = processor.process(thirdTx);
        txDao.saveTx(firstTx);
        Result res3 = processor.process(firstTx);

        // then
        assertThat(res1.isOk()).isFalse();
        assertThat(res2.isOk()).isFalse();
        assertThat(res3.isOk()).isTrue();

        assertThat(orphanedTxDao.all())
                .hasSize(2)
                .contains(secondTx.getHash(), thirdTx.getHash());
    }

    @Test
    public void processBlock() throws Exception {
        // TODO
    }

    @Test
    @FlywayTest
    public void deOrphan() throws Exception {
        // given
        Tx firstTx = Tx.builder()
                .setCoinbase(true)
                .add(output(RndUtil.rndPubKey(), 2000))
                .build();

        Tx secondTx = Tx.builder()
                .setCoinbase(false)
                .add(input(firstTx.getHash(), 0, rndBytes(32)), rndPrivKey())
                .add(output(RndUtil.rndPubKey(), 2000))
                .build();

        Tx thirdTx = Tx.builder()
                .setCoinbase(false)
                .add(input(secondTx.getHash(), 0, rndBytes(32)), rndPrivKey())
                .build();

        // when
        Result res1 = process(secondTx);
        Result res2 = process(thirdTx);
        Result res3 = process(firstTx);

        // then
        assertThat(res1.isOk()).isFalse();
        assertThat(res2.isOk()).isFalse();
        assertThat(res3.isOk()).isTrue();

        assertThat(deOrphanedTxs)
                .hasSize(3)
                .contains(firstTx, secondTx, thirdTx);

        assertThat(orphanedTxDao.all())
                .isEmpty();
    }

    private Result process(Tx tx) {
        txDao.saveTx(tx);
        Result res = processor.process(tx);
        if (res.isOk()) {
            processor.deOrphan(tx, null,
                    deOrphanedTxs::add,
                    deOrphanedBlocks::add);
        }
        return res;
    }

}