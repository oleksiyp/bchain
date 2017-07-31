package bchain.dao.sqlite;

import bchain.util.ExtendedJdbcTemplate;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.File;

@Component
public class SqliteTestConfig {
    @Bean
    public SqliteTxDao txDao() {
        return new SqliteTxDao();
    }

    @Bean
    public SqliteBlockDao blockDao() { return new SqliteBlockDao(); }

    @PreDestroy
    public void removeDb() {
        new File("test.db").delete();
    }

    @Bean
    public DataSource dataSource() {
        SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setSuppressClose(true);
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite:test.db");
        return ds;
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

