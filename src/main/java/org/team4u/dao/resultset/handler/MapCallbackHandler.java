package org.team4u.dao.resultset.handler;

import org.apache.commons.dbutils.ResultSetHandler;
import org.team4u.dao.util.DbUtil;
import org.team4u.kit.core.action.Callback;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author Jay Wu
 */
public class MapCallbackHandler<T extends Map<String, Object>> implements ResultSetHandler<T> {
    /**
     * The Class of beans produced by this handler.
     */
    protected final Class<T> type;

    protected final Callback<T> callback;

    public MapCallbackHandler(Class<T> type, Callback<T> callback) {
        this.type = type;
        this.callback = callback;
    }

    @Override
    public T handle(ResultSet rs) throws SQLException {
        while (rs.next()) {
            callback.invoke(DbUtil.toMap(type, rs, false));
        }

        return null;
    }
}