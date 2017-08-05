package bchain.dao.sqlite;

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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SqliteTestConfig.class, JdbcTemplateAutoConfiguration.class})
@TestExecutionListeners(value = {DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class})
@TestPropertySource(properties = "sqlite.file=test.db")
public class SqliteTxDaoTest {

    @Autowired
    SqliteTxDao txDao;

    @Test
    @FlywayTest
    public void saveTx() {
        // given
        Tx tx = TestObjects.TEST_TX1;

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
        Tx tx = TestObjects.TEST_TX2;

        // when
        boolean before = txDao.hasTx(tx.getHash());
        boolean saveFirst = txDao.saveTx(tx);
        boolean after = txDao.hasTx(tx.getHash());
        boolean saveSecond = txDao.saveTx(tx);
        boolean more = txDao.hasTx(tx.getHash());

        // then
        assertThat(before).isEqualTo(false);
        assertThat(saveFirst).isEqualTo(true);
        assertThat(after).isEqualTo(true);
        assertThat(saveSecond).isEqualTo(false);
        assertThat(more).isEqualTo(true);
    }

}