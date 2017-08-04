package org.team4u.dao.resultset.handler;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.RowProcessor;
import org.team4u.kit.core.action.Callback;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Jay Wu
 */
public class BeanCallbackHandler<T> implements ResultSetHandler<T> {
    /**
     * The Class of beans produced by this handler.
     */
    protected final Class<T> type;

    /**
     * The RowProcessor implementation to use when converting rows
     * into beans.
     */
    protected final RowProcessor convert;

    protected final Callback<T> callback;

    public BeanCallbackHandler(Class<T> type, RowProcessor convert, Callback<T> callback) {
        this.type = type;
        this.convert = convert;
        this.callback = callback;
    }

    @Override
    public T handle(ResultSet rs) throws SQLException {
        while (rs.next()) {
            callback.invoke(convert.toBean(rs, this.type));
        }

        return null;
    }
}