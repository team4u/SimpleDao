package org.team4u.dao.util;

import com.xiaoleilu.hutool.util.StrUtil;
import org.team4u.kit.core.util.MapUtil;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author Jay Wu
 */
public class DbUtil {

    @SuppressWarnings("unchecked")
    public static <T extends Map<String, Object>> T toMap(Class<T> mapClass,
                                                          ResultSet rs,
                                                          boolean camelCase) throws SQLException {
        T map = (T) MapUtil.<String, Object>newInstance(mapClass);
        ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();

        for (int i = 1; i <= cols; i++) {
            String columnName = rsmd.getColumnLabel(i);
            if (null == columnName || 0 == columnName.length()) {
                columnName = rsmd.getColumnName(i);
            }

            columnName = columnName.toLowerCase();

            if (camelCase) {
                columnName = StrUtil.toCamelCase(columnName);
            }
            map.put(columnName, rs.getObject(i));
        }

        return map;
    }
}