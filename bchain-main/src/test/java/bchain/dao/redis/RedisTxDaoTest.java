package bchain.dao.redis;

import bchain.dao.TxDaoTest;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RedisConfig.class, RedisAutoConfiguration.class})
@TestExecutionListeners(value = {DependencyInjectionTestExecutionListener.class})
@TestPropertySource(properties = "redis.port=55343")
public class RedisTxDaoTest extends TxDaoTest {
}
