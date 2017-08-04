package org.team4u.dao.core;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Jay Wu
 */
public interface ConnectionCallback<T> {

    T doInConnection(Connection connection) throws SQLException;
}