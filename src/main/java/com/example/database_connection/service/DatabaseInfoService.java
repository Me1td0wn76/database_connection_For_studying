package com.example.database_connection.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

@Service
public class DatabaseInfoService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * データベース接続情報を取得
     */
    public Map<String, Object> getDatabaseInfo() {
        Map<String, Object> info = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            info.put("connected", true);
            info.put("databaseProductName", metaData.getDatabaseProductName());
            info.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            info.put("driverName", metaData.getDriverName());
            info.put("driverVersion", metaData.getDriverVersion());
            info.put("url", metaData.getURL());
            info.put("userName", metaData.getUserName());

        } catch (SQLException e) {
            info.put("connected", false);
            info.put("error", e.getMessage());
        }

        return info;
    }

    /**
     * コネクションプールの状態を取得
     */
    public Map<String, Object> getConnectionPoolInfo() {
        Map<String, Object> poolInfo = new HashMap<>();

        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();

            poolInfo.put("poolName", hikariDataSource.getPoolName());
            poolInfo.put("activeConnections", poolMXBean.getActiveConnections());
            poolInfo.put("idleConnections", poolMXBean.getIdleConnections());
            poolInfo.put("totalConnections", poolMXBean.getTotalConnections());
            poolInfo.put("threadsAwaitingConnection", poolMXBean.getThreadsAwaitingConnection());
            poolInfo.put("maxPoolSize", hikariDataSource.getMaximumPoolSize());
            poolInfo.put("minIdle", hikariDataSource.getMinimumIdle());
        }

        return poolInfo;
    }

    /**
     * カスタムSQLクエリを実行
     */
    public List<Map<String, Object>> executeQuery(String sql) {
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            throw new RuntimeException("クエリ実行エラー: " + e.getMessage());
        }
    }

    /**
     * テーブル一覧を取得
     */
    public List<String> getTableNames() {
        String query = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC'";
        return jdbcTemplate.queryForList(query, String.class);
    }

    /**
     * 接続テスト
     */
    public boolean testConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
}
