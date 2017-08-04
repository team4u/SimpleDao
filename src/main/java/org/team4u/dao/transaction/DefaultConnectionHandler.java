package org.team4u.dao.transaction;

import org.apache.commons.dbutils.DbUtils;
import org.team4u.kit.core.error.ExceptionUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Jay Wu
 */
public enum DefaultConnectionHandler implements ConnectionHandler {

    INSTANCE;

    @Override
    public Connection getConnection(DataSource dataSource) throws SQLException {
        Transaction transaction = Transactions.get();
        // 存在事务,由Transaction托管
        if (transaction != null) {
            return transaction.getConnection(dataSource);
        }

        return dataSource.getConnection();
    }

    @Override
    public void releaseConnection(Connection connection, DataSource dataSource) {
        if (connection == null) {
            return;
        }

        // 存在事务,由Transaction托管
        if (Transactions.get() != null) {
            return;
        }

        // 无事务
        try {
            if (!connection.getAutoCommit()) {
                connection.commit();
            }

            DbUtils.closeQuietly(connection);
        } catch (SQLException e) {
            DbUtils.rollbackAndCloseQuietly(connection);
            throw ExceptionUtil.toRuntimeException(e);
        }
    }
}