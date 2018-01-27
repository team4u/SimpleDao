package org.team4u.dao.resultset.handler;

import cn.hutool.core.util.ClassUtil;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.team4u.dao.resultset.processor.CamelCaseProcessor;
import org.team4u.dao.resultset.processor.EntityProcessor;
import org.team4u.kit.core.action.Callback;
import org.team4u.kit.core.lang.Pair;
import org.team4u.sql.builder.entity.Entity;
import org.team4u.sql.builder.entity.EntityManager;
import org.team4u.sql.builder.entity.builder.EntitySqlBuilder;

import java.util.List;
import java.util.Map;

public class DefaultResultSetHandlerSelector implements ResultSetHandlerSelector {

    protected final static RowProcessor CAMEL_CASE_ROW_PROCESSOR =
            new BasicRowProcessor(CamelCaseProcessor.DEFAULT_INSTANCE);

    protected EntityManager entityManager;

    protected RowProcessor entityRowProcessor;

    public DefaultResultSetHandlerSelector() {
        this(EntitySqlBuilder.DEFAULT_ENTITY_MANAGER);
    }

    public DefaultResultSetHandlerSelector(EntityManager entityManager) {
        this(entityManager, new BasicRowProcessor(new EntityProcessor(entityManager)));
    }

    public DefaultResultSetHandlerSelector(EntityManager entityManager, RowProcessor entityRowProcessor) {
        this.entityManager = entityManager;
        this.entityRowProcessor = entityRowProcessor;
    }

    @Override
    public <T> Pair<Entity.Column, ResultSetHandler<Object>> selectForPK(Class<T> clazz) {
        if (!entityManager.isEntity(clazz)) {
            return null;
        }

        Entity<?> entity = entityManager.createIfNotExist(clazz);
        if (entity.getIdColumns().size() != 1) {
            return null;
        }

        Entity.Column idColumn = entity.getIdColumns().get(0);

        if (!idColumn.isAutoId()) {
            return null;
        }

        //noinspection unchecked
        return new Pair<Entity.Column, ResultSetHandler<Object>>(idColumn, (ResultSetHandler<Object>) selectForObject(idColumn.getProperty().getType()));
    }

    @Override
    public <T> ResultSetHandler<T> selectForObject(Class<T> clazz) {
        if (entityManager.isEntity(clazz)) {
            return new BeanHandler<T>(clazz, entityRowProcessor);
        } else if (ClassUtil.isSimpleValueType(clazz)) {
            return new PrimitiveWrapperHandler<T>(clazz);
        } else if (Map.class.isAssignableFrom(clazz)) {
            //noinspection unchecked
            return new MapHandler(clazz);
        } else {
            return new BeanHandler<T>(clazz, CAMEL_CASE_ROW_PROCESSOR);
        }
    }

    @Override
    public <T> ResultSetHandler<List<T>> selectForList(Class<T> clazz) {
        if (entityManager.isEntity(clazz)) {
            return new BeanListHandler<T>(clazz, entityRowProcessor);
        } else if (ClassUtil.isSimpleValueType(clazz)) {
            return new PrimitiveWrapperListHandler<T>(clazz);
        } else if (Map.class.isAssignableFrom(clazz)) {
            //noinspection unchecked
            return new MapListHandler(clazz);
        } else {
            return new BeanListHandler<T>(clazz, CAMEL_CASE_ROW_PROCESSOR);
        }
    }

    @Override
    public <T> ResultSetHandler<T> selectForCallback(Class<T> clazz, Callback<T> callback) {
        if (entityManager.isEntity(clazz)) {
            return new BeanCallbackHandler<T>(clazz, entityRowProcessor, callback);
        } else if (ClassUtil.isSimpleValueType(clazz)) {
            return new PrimitiveWrapperCallbackHandler<T>(clazz, callback);
        } else if (Map.class.isAssignableFrom(clazz)) {
            //noinspection unchecked
            return new MapCallbackHandler(clazz, callback);
        } else {
            return new BeanCallbackHandler<T>(clazz, CAMEL_CASE_ROW_PROCESSOR, callback);
        }
    }
}