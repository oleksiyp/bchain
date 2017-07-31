package bchain.dao.sqlite;

import bchain.domain.Block;
import bchain.domain.Hash;
import bchain.domain.Tx;
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

import static bchain.domain.Hash.hashOf;
import static bchain.domain.PubKey.pubKey;
import static bchain.domain.TxInput.input;
import static bchain.domain.TxOutput.output;
import static bchain.util.RndUtil.rndBytes;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SqliteTestConfig.class, JdbcTemplateAutoConfiguration.class})
@TestExecutionListeners(value = {DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class})
public class SqliteBlockDaoTest {
    @Autowired
    SqliteBlockDao blockDao;

    @Test
    @FlywayTest
    public void hasBlock() throws Exception {
        // given
        Block block = testBlock1();

        // when
        boolean before = blockDao.hasBlock(block.getHash());
        blockDao.saveBlock(block);
        boolean after = blockDao.hasBlock(block.getHash());

        // then
        assertThat(before).isEqualTo(false);
        assertThat(after).isEqualTo(true);
    }

    @Test
    @FlywayTest
    public void saveBlock() throws Exception {
        // given
        Block block = testBlock1();

        // when
        blockDao.saveBlock(block);

        // then
        assertThat(blockDao.all())
                .hasSize(1)
                .contains(block);
    }

    @Test
    @FlywayTest
    public void saveTwoBlocks() throws Exception {
        // given
        Block block = testBlock1();
        Block block2 = testBlock(block.getHash());

        // when
        blockDao.saveBlock(block);
        blockDao.saveBlock(block2);

        // then
        assertThat(blockDao.all())
                .hasSize(2)
                .contains(block, block2);
    }


    private Block testBlock1() {
        return testBlock(hashOf("abc"));
    }

    private Block testBlock(Hash prevBlockHash) {
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