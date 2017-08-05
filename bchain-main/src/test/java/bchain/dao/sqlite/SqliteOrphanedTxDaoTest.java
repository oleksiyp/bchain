package bchain.dao.sqlite;

import bchain.dao.OrphanedTxDao;
import bchain.dao.TxDao;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SqliteTestConfig.class, JdbcTemplateAutoConfiguration.class})
@TestExecutionListeners(value = {DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class})
@TestPropertySource(properties = "sqlite.file=test.db")
public class SqliteOrphanedTxDaoTest {
    @Autowired
    TxDao txDao;

    @Autowired
    OrphanedTxDao orphanedTxDao;

    @Test
    @FlywayTest
    public void addRemove() throws Exception {
        // given
        Tx tx = TestObjects.TEST_TX1;
        Hash hash = tx.getHash();
        txDao.saveTx(tx);

        // when
        boolean beforeAdd = orphanedTxDao.isOrphaned(hash);
        orphanedTxDao.add(hash);
        boolean afterAdd = orphanedTxDao.isOrphaned(hash);
        orphanedTxDao.add(hash);
        boolean afterAdd2 = orphanedTxDao.isOrphaned(hash);
        orphanedTxDao.remove(hash);
        boolean afterRemove = orphanedTxDao.isOrphaned(hash);
        orphanedTxDao.remove(hash);
        boolean afterRemove2 = orphanedTxDao.isOrphaned(hash);

        // then
        assertThat(beforeAdd).isFalse();
        assertThat(afterAdd).isTrue();
        assertThat(afterAdd2).isTrue();
        assertThat(afterRemove).isFalse();
        assertThat(afterRemove2).isFalse();
    }
}