package bchain.dao.sqlite;

import bchain.dao.TxDaoTest;
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
import org.springframework.test.context.TestPropertySource;
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
@TestPropertySource(properties = "sqlite.file=test.db")
public class SqliteBlockDaoTest extends TxDaoTest {
}