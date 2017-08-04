package org.team4u.dao.resultset.handler;

import org.apache.commons.dbutils.handlers.AbstractListHandler;
import org.team4u.dao.util.DbUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author Jay Wu
 */
public class MapListHandler<T extends Map<String, Object>> extends AbstractListHandler<T> {

    protected final Class<T> valueClassRef;
    protected boolean camelCase;

    public MapListHandler(Class<T> valueClassRef) {
        this(valueClassRef, false);
    }

    public MapListHandler(Class<T> valueClassRef, boolean camelCase) {
        this.valueClassRef = valueClassRef;
        this.camelCase = camelCase;
    }

    @Override
    protected T handleRow(ResultSet rs) throws SQLException {
        return DbUtil.toMap(valueClassRef, rs, camelCase);
    }
}