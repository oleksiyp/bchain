package bchain.dao.sqlite;

import bchain.util.ExtendedJdbcTemplate;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.File;

@Component
@Import(SqliteConfig.class)
public class SqliteTestConfig {

    @PreDestroy
    public void removeDb() {
        new File("test.db").delete();
    }

    @Bean
    public Flyway flyway(DataSource ds) {
        Flyway flyway = new Flyway();
        flyway.setDataSource(ds);
        flyway.clean();
        return flyway;
    }

    @Bean
    public ExtendedJdbcTemplate jdbcTemplate(DataSource ds) {
        return new ExtendedJdbcTemplate(ds);
    }
}

