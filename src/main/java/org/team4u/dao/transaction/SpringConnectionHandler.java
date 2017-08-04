package org.team4u.dao.transaction;

import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Jay Wu
 */
public enum SpringConnectionHandler implements ConnectionHandler {

    INSTANCE;

    @Override
    public Connection getConnection(DataSource dataSource) throws SQLException {
        return DataSourceUtils.getConnection(dataSource);
    }

    @Override
    public void releaseConnection(Connection connection, DataSource dataSource) {
        DataSourceUtils.releaseConnection(connection, dataSource);
    }
}