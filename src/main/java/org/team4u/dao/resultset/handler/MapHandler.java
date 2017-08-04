package org.team4u.dao.resultset.handler;

import org.apache.commons.dbutils.ResultSetHandler;
import org.team4u.dao.util.DbUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author Jay Wu
 */
public class MapHandler<T extends Map<String, Object>> implements ResultSetHandler<T> {

    protected final Class<T> valueClassRef;
    protected boolean camelCase;

    public MapHandler(Class<T> valueClassRef) {
        this(valueClassRef, false);
    }

    public MapHandler(Class<T> valueClassRef, boolean camelCase) {
        this.camelCase = camelCase;
        this.valueClassRef = valueClassRef;
    }

    @Override
    public T handle(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return DbUtil.toMap(valueClassRef, rs, camelCase);
        }

        return null;
    }
}