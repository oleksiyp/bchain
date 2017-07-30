package bchain.dao.sqlite;

import bchain.dao.TestDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class SqliteTestDao implements TestDao {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDatasource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void addAbc(String abc) {
        jdbcTemplate.update("insert into abc(val) values (?)", abc);
    }
}
