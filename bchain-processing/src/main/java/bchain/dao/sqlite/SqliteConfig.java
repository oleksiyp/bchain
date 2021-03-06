package bchain.dao.sqlite;

import bchain.dao.*;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.function.Supplier;

@Component
public class SqliteConfig {

    @Value("${sqlite.file}")
    String file;

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("bchain/dao/sqlite");
        return flyway;
    }

    @Bean
    public DataSource dataSource() {
        SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setSuppressClose(true);
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite:" + file);
        return ds;
    }

    @Bean
    public TxDao testDao() {
        return new SqliteTxDao();
    }

    @Bean
    public BlockDao blockDao() {
        return new SqliteBlockDao();
    }

    @Bean
    public OrphanedTxDao orphanedTxDao() {
        return new SqliteOrphanedTxDao();
    }

    @Bean
    public OrphanedBlockDao orphanedBlockDao() {
        return new SqliteOrphanedBlockDao();
    }

    @Bean
    public UnspentDao unspentDao() {
        return new SqliteUnspentDao();
    }

    @Bean
    public BlockLevelDao blockLevelDao() {
        return new SqliteBlockLevelDao();
    }

    @Bean
    public PendingTxDao pendingTxDao() {
        return new SqlitePendingTxDao();
    }

    @Bean
    public RefsDao refsDao() {
        return new SqliteRefsDao();
    }

    public static Supplier<RuntimeException> error(String msg) {
        return () -> new RuntimeException(msg);
    }

}
