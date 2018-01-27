package org.team4u.dao.resultset.handler;

import cn.hutool.core.convert.Convert;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * <code>ResultSetHandler</code> implementation that converts one
 * <code>ResultSet</code> column into an Object. This class is thread safe.
 *
 * @param <T> The type of the scalar
 * @author Jay Wu
 * @see org.apache.commons.dbutils.ResultSetHandler
 */
public class PrimitiveWrapperHandler<T> implements ResultSetHandler<T> {

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
    public PrimitiveWrapperHandler(Class<T> valueClassRef) {
        this(valueClassRef, 1, null);
    }

    /**
     * Creates a new instance of PrimitiveWrapperHandler.
     *
     * @param columnIndex The index of the column to retrieve from the
     *                    <code>ResultSet</code>.
     */
    public PrimitiveWrapperHandler(Class<T> valueClassRef, int columnIndex) {
        this(valueClassRef, columnIndex, null);
    }

    /**
     * Creates a new instance of PrimitiveWrapperHandler.
     *
     * @param columnName The name of the column to retrieve from the
     *                   <code>ResultSet</code>.
     */
    public PrimitiveWrapperHandler(Class<T> valueClassRef, String columnName) {
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
    protected PrimitiveWrapperHandler(Class<T> valueClassRef, int columnIndex, String columnName) {
        this.valueClassRef = valueClassRef;
        this.columnIndex = columnIndex;
        this.columnName = columnName;
    }

    /**
     * Returns one <code>ResultSet</code> column as an object via the
     * <code>ResultSet.getObject()</code> method that performs type
     * conversions.
     *
     * @param rs <code>ResultSet</code> to process.
     * @return The column or <code>null</code> if there are no rows in
     * the <code>ResultSet</code>.
     * @throws SQLException       if a database access error occurs
     * @throws ClassCastException if the class datatype does not match the column type
     * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
     */
    // We assume that the user has picked the correct type to match the column
    // so getObject will return the appropriate type and the cast will succeed.
    @SuppressWarnings("unchecked")
    @Override
    public T handle(ResultSet rs) throws SQLException {
        Object value = null;

        if (rs.next()) {
            if (this.columnName == null) {
                value = rs.getObject(this.columnIndex);
            } else {
                value = rs.getObject(this.columnName);
            }

            return Convert.convert(valueClassRef, value);
        }

        return null;
    }
}