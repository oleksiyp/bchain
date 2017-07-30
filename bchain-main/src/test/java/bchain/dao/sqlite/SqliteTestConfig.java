package bchain.dao.sqlite;

import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class SqliteTestConfig {
    @Bean
    public SqliteTxDao txDao() {
        return new SqliteTxDao();
    }

    @Bean
    public DataSource dataSource() {
        SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setSuppressClose(true);
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite:test.db");
        return ds;
    }

}

