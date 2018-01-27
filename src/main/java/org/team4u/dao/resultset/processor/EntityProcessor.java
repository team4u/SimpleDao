package org.team4u.dao.resultset.processor;


import cn.hutool.core.lang.Assert;
import org.apache.commons.dbutils.BeanProcessor;
import org.team4u.sql.builder.entity.Entity;
import org.team4u.sql.builder.entity.EntityManager;
import org.team4u.sql.builder.entity.builder.EntitySqlBuilder;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Jay Wu
 */
public class EntityProcessor extends BeanProcessor {

    private EntityManager entityManager;

    private ThreadLocal<Class<?>> typeThreadLocal = new ThreadLocal<Class<?>>();

    public EntityProcessor() {
        this(EntitySqlBuilder.DEFAULT_ENTITY_MANAGER);
    }

    public EntityProcessor(EntityManager entityManager) {
        super();
        this.entityManager = entityManager;
    }

    public EntityProcessor(Map<String, String> columnToPropertyOverrides, EntityManager entityManager) {
        super(columnToPropertyOverrides);
        this.entityManager = entityManager;
    }

    @Override
    public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
        typeThreadLocal.set(type);
        return super.toBeanList(rs, type);
    }

    @Override
    public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
        typeThreadLocal.set(type);
        return super.toBean(rs, type);
    }

    @Override
    protected int[] mapColumnsToProperties(ResultSetMetaData rsmd, Field[] props)
            throws SQLException {
        int cols = rsmd.getColumnCount();
        int[] columnToProperty = new int[cols + 1];
        Arrays.fill(columnToProperty, PROPERTY_NOT_FOUND);

        Entity entity = entityManager.createIfNotExist(typeThreadLocal.get());
        Assert.notNull(entity, String.format("Can't find entity(table=%s)", entity.getTable()));

        for (int col = 1; col <= cols; col++) {
            String columnName = rsmd.getColumnLabel(col);
            if (null == columnName || 0 == columnName.length()) {
                columnName = rsmd.getColumnName(col);
            }

            Entity.Column column = entity.getColumnWithColumnName(columnName);

            if (column == null) {
                continue;
            }

            String propertyName = column.getProperty().getName();

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