package org.team4u.dao.resultset.handler;

import com.xiaoleilu.hutool.convert.Convert;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.AbstractListHandler;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * <code>ResultSetHandler</code> implementation that converts one
 * <code>ResultSet</code> column into an Object. This class is thread safe.
 *
 * @param <T> The type of the scalar
 * @author Jay Wu
 * @see ResultSetHandler
 */
public class PrimitiveWrapperListHandler<T> extends AbstractListHandler<T> {

    /**
     * The column number to retrieve.
     */
    protected final int columnIndex;

    /**
     * The column name to retrieve.  Either columnName or columnIndex
     * will be used but never both.
     */
    protected final String columnName;

    protected final Class<T> valueClassRef;

    /**
     * Creates a new instance of PrimitiveWrapperHandler.  The first column will
     * be returned from <code>handle()</code>.
     */
    public PrimitiveWrapperListHandler(Class<T> valueClassRef) {
        this(valueClassRef, 1, null);
    }

    /**
     * Creates a new instance of PrimitiveWrapperHandler.
     *
     * @param columnIndex The index of the column to retrieve from the
     *                    <code>ResultSet</code>.
     */
    public PrimitiveWrapperListHandler(Class<T> valueClassRef, int columnIndex) {
        this(valueClassRef, columnIndex, null);
    }

    /**
     * Creates a new instance of PrimitiveWrapperHandler.
     *
     * @param columnName The name of the column to retrieve from the
     *                   <code>ResultSet</code>.
     */
    public PrimitiveWrapperListHandler(Class<T> valueClassRef, String columnName) {
        this(valueClassRef, 1, columnName);
    }

    /**
     * Helper constructor
     *
     * @param valueClassRef The class of value
     * @param columnIndex   The index of the column to retrieve from the
     *                      <code>ResultSet</code>.
     * @param columnName    The name of the column to retrieve from the
     *                      <code>ResultSet</code>.
     */
    protected PrimitiveWrapperListHandler(Class<T> valueClassRef, int columnIndex, String columnName) {
        this.valueClassRef = valueClassRef;
        this.columnIndex = columnIndex;
        this.columnName = columnName;
    }

    @Override
    protected T handleRow(ResultSet rs) throws SQLException {
        Object value;

        if (this.columnName == null) {
            value = rs.getObject(this.columnIndex);
        } else {
            value = rs.getObject(this.columnName);
        }

        return Convert.convert(valueClassRef, value);
    }
}