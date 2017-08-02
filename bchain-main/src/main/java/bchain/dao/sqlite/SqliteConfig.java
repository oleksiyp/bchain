package bchain.dao.sqlite;

import bchain.dao.*;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class SqliteConfig {

    @Bean
    public DataSource dataSource() {
        SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setSuppressClose(true);
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite:bchain.db");
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

}
