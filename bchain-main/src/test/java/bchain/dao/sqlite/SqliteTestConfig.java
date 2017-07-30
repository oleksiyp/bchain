package bchain.dao.sqlite;

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

    @Bean(destroyMethod = "clean")
    public Flyway flyway(DataSource ds) {
        Flyway flyway = new Flyway();
        flyway.setDataSource(ds);
        flyway.clean();
        return flyway;
    }

}

