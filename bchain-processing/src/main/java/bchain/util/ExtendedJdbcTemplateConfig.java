package bchain.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class ExtendedJdbcTemplateConfig {
    private final DataSource dataSource;

    public ExtendedJdbcTemplateConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public ExtendedJdbcTemplate jdbcTemplate() {
        return new ExtendedJdbcTemplate(this.dataSource);
    }
}
