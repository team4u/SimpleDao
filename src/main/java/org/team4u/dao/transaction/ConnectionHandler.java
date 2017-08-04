package org.team4u.dao.transaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Jay Wu
 */
public interface ConnectionHandler {

    /**
     * 根据数据源获取数据库连接
     */
    Connection getConnection(DataSource dataSource) throws SQLException;

    /**
     * 释放数据库连接
     */
    void releaseConnection(Connection connection, DataSource dataSource);
}