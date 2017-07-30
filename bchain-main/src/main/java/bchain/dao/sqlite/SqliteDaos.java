package bchain.dao.sqlite;

import bchain.dao.TestDao;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class SqliteDaos {

    @Bean
    public DataSource dataSource() {
        SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setSuppressClose(true);
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite:bchain.db");
        return ds;
    }

    @Bean
    public TestDao testDao() {
        return new SqliteTestDao();
    }
}
