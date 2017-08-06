package bchain.dao.sqlite;

import bchain.domain.Block;
import bchain.util.ExtendedJdbcTemplateConfig;
import org.flywaydb.test.annotation.FlywayTest;
import org.flywaydb.test.junit.FlywayTestExecutionListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SqliteTestConfig.class, ExtendedJdbcTemplateConfig.class})
@TestExecutionListeners(value = {DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class})
@TestPropertySource(properties = "sqlite.file=test.db")
public class SqliteBlockDaoTest {
    @Autowired
    SqliteBlockDao blockDao;

    @Test
    @FlywayTest
    public void hasBlock() throws Exception {
        // given
        Block block = TestObjects.TEST_BLOCK1;

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
        Block block = TestObjects.TEST_BLOCK1;

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
        Block block = TestObjects.TEST_BLOCK1;
        Block block2 = TestObjects.TEST_BLOCK2;

        // when
        blockDao.saveBlock(block);
        blockDao.saveBlock(block2);

        // then
        assertThat(blockDao.all())
                .hasSize(2)
                .contains(block, block2);
    }


}