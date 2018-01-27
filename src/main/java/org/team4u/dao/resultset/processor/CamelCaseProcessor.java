package org.team4u.dao.resultset.processor;


import cn.hutool.core.util.StrUtil;
import org.apache.commons.dbutils.BeanProcessor;

import java.lang.reflect.Field;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * @author Jay Wu
 */
public class CamelCaseProcessor extends BeanProcessor {

    public final static CamelCaseProcessor DEFAULT_INSTANCE = new CamelCaseProcessor();

    @Override
    protected int[] mapColumnsToProperties(ResultSetMetaData rsmd, Field[] props)
            throws SQLException {
        int cols = rsmd.getColumnCount();
        int[] columnToProperty = new int[cols + 1];
        Arrays.fill(columnToProperty, PROPERTY_NOT_FOUND);

        for (int col = 1; col <= cols; col++) {
            String columnName = rsmd.getColumnLabel(col);
            if (null == columnName || 0 == columnName.length()) {
                columnName = rsmd.getColumnName(col);
            }

            String propertyName = StrUtil.toCamelCase(columnName);
            for (int i = 0; i < props.length; i++) {
                if (propertyName.equalsIgnoreCase(props[i].getName())) {
                    columnToProperty[col] = i;
                    break;
                }
            }
        }

        return columnToProperty;
    }
}