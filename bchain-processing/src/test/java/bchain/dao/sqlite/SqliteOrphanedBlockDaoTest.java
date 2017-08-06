package bchain.dao.sqlite;

import bchain.dao.BlockDao;
import bchain.dao.OrphanedBlockDao;
import bchain.domain.Block;
import bchain.domain.Hash;
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

import static bchain.dao.sqlite.TestObjects.TEST_BLOCK1;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SqliteTestConfig.class, ExtendedJdbcTemplateConfig.class})
@TestExecutionListeners(value = {DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class})
@TestPropertySource(properties = "sqlite.file=test.db")
public class SqliteOrphanedBlockDaoTest {
    @Autowired
    BlockDao blockDao;

    @Autowired
    OrphanedBlockDao orphanedBlockDao;

    @Test
    @FlywayTest
    public void addRemove() throws Exception {
        // given
        Block block = TEST_BLOCK1;
        Hash hash = block.getHash();
        blockDao.saveBlock(block);

        // when
        boolean beforeAdd = orphanedBlockDao.isOrphaned(hash);
        orphanedBlockDao.add(hash);
        boolean afterAdd = orphanedBlockDao.isOrphaned(hash);
        orphanedBlockDao.add(hash);
        boolean afterAdd2 = orphanedBlockDao.isOrphaned(hash);
        orphanedBlockDao.remove(hash);
        boolean afterRemove = orphanedBlockDao.isOrphaned(hash);
        orphanedBlockDao.remove(hash);
        boolean afterRemove2 = orphanedBlockDao.isOrphaned(hash);

        // then
        assertThat(beforeAdd).isFalse();
        assertThat(afterAdd).isTrue();
        assertThat(afterAdd2).isTrue();
        assertThat(afterRemove).isFalse();
        assertThat(afterRemove2).isFalse();
    }
}