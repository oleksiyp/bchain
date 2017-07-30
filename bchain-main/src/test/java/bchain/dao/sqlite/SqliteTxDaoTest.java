package bchain.dao.sqlite;

import bchain.domain.Tx;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(DependencyInjectionTestExecutionListener.class)
@Import(SqliteTestConfig.class)
public class SqliteTxDaoTest extends BaseDBHelper {

    @Autowired
    SqliteTxDao txDao;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    @FlywayTest
    public void save() {
        // given
        Tx tx = Tx.builder()
                .build();

        // when
        txDao.saveTx(tx);

        // then

    }
}