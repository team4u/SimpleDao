package org.team4u.dao.core;

import com.xiaoleilu.hutool.util.CollectionUtil;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.team4u.dao.resultset.handler.DefaultResultSetHandlerSelector;
import org.team4u.dao.resultset.handler.ResultSetHandlerSelector;
import org.team4u.dao.transaction.ConnectionHandler;
import org.team4u.dao.transaction.DefaultConnectionHandler;
import org.team4u.kit.core.action.Callback;
import org.team4u.kit.core.action.Function;
import org.team4u.kit.core.error.ExceptionUtil;
import org.team4u.kit.core.lang.EmptyValue;
import org.team4u.kit.core.lang.Pager;
import org.team4u.kit.core.lang.Pair;
import org.team4u.kit.core.util.AssertUtil;
import org.team4u.kit.core.util.CollectionExUtil;
import org.team4u.kit.core.util.ValueUtil;
import org.team4u.sql.builder.Sql;
import org.team4u.sql.builder.dialect.Dialect;
import org.team4u.sql.builder.dialect.DialectManager;
import org.team4u.sql.builder.entity.Entity;
import org.team4u.sql.builder.entity.builder.EntitySqlBuilder;
import org.team4u.sql.builder.util.SqlBuilders;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jay Wu
 */
public class SimpleDao implements Dao {

    protected QueryRunner queryRunner;
    protected DataSource dataSource;
    protected Dialect dialect;
    protected ResultSetHandlerSelector resultSetHandlerSelector;
    protected ConnectionHandler connectionHandler;

    public SimpleDao(DataSource dataSource,
                     ResultSetHandlerSelector resultSetHandlerSelector,
                     ConnectionHandler connectionHandler) {
        this.dataSource = dataSource;
        this.resultSetHandlerSelector = resultSetHandlerSelector;
        this.connectionHandler = connectionHandler;

        dialect = DialectManager.INSTANCE.getDialect(this.dataSource);
        AssertUtil.notNull(dialect, "Not support database:" + dataSource);
        queryRunner = new QueryRunner();
    }

    public SimpleDao(DataSource dataSource, ConnectionHandler connectionHandler) {
        this(dataSource, new DefaultResultSetHandlerSelector(), connectionHandler);
    }

    public SimpleDao(DataSource dataSource, ResultSetHandlerSelector resultSetHandlerSelector) {
        this(dataSource, resultSetHandlerSelector, DefaultConnectionHandler.INSTANCE);
    }

    public SimpleDao(DataSource dataSource) {
        this(dataSource, DefaultConnectionHandler.INSTANCE);
    }

    @Override
    public <T> T queryWithPkForObject(Class<T> resultClass, Object... ids) {
        return queryForObject(resultClass, SqlBuilders.select(resultClass).withPK(ids).create());
    }

    @Override
    public <T> T queryForObject(Class<T> resultClass, Sql sql) {
        return query(resultSetHandlerSelector.selectForObject(resultClass), sql);
    }

    @Override
    public <T> T queryForObject(EntitySqlBuilder<T> sqlBuilder) {
        return (T) queryForObject(sqlBuilder.getEntity().getClassRef(), sqlBuilder.create());
    }

    @Override
    public <T> List<T> queryForList(Class<T> resultClass, Sql sql) {
        return query(resultSetHandlerSelector.selectForList(resultClass), sql);
    }

    @Override
    public <T> List<T> queryForList(EntitySqlBuilder<T> sqlBuilder) {
        return queryForList(sqlBuilder, null);
    }

    @Override
    public <T> T query(final ResultSetHandler<T> handler, final Sql sql) {
        return execute(new ConnectionCallback<T>() {
            @Override
            public T doInConnection(Connection connection) throws SQLException {
                return queryRunner.query(connection, sql.getContent(), handler, sql.getParams());
            }
        });
    }

    @Override
    public <T> List<T> queryForList(Class<T> resultClass, Sql sql, Pager pager) {
        if (pager != null && pager.isAutoCount()) {
            pager.setRecordCount(count(sql));
        }

        return queryForList(resultClass, createPagerSql(sql, pager));
    }

    @Override
    public <T> List<T> queryForList(EntitySqlBuilder<T> sqlBuilder, Pager pager) {
        return queryForList(sqlBuilder.getEntity().getClassRef(), sqlBuilder.create(), pager);
    }

    @Override
    public int count(Sql sql) {
        Sql countSql = new Sql(dialect.createCountSelect(sql.getContent()), sql.getParams());
        return queryForObject(Integer.class, countSql);
    }

    @Override
    public <T> void each(Class<T> resultClass, Sql sql, Callback<T> callback) {
        each(resultClass, sql, null, callback);
    }

    @Override
    public <T> void each(EntitySqlBuilder<T> sqlBuilder, Callback<T> callback) {
        each(sqlBuilder, null, callback);
    }

    @Override
    public <T> void each(Class<T> resultClass, Sql sql, Pager pager, Callback<T> callback) {
        query(resultSetHandlerSelector.selectForCallback(resultClass, callback), createPagerSql(sql, pager));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void each(EntitySqlBuilder<T> sqlBuilder, Pager pager, Callback<T> callback) {
        Class<T> clazz = (Class<T>) sqlBuilder.getEntity().getClassRef();
        each(clazz, sqlBuilder.create(), pager, callback);
    }

    @Override
    public <T> T insert(final T entity) {
        return insert(entity, null, false);
    }

    @Override
    public <T> T insert(final T entity, String activatedColumns, boolean ignoreNull) {
        if (entity == null) {
            return null;
        }

        final Sql sql = SqlBuilders.insert(entity)
                .columns(activatedColumns)
                .setInsertIgnoreNull(ignoreNull)
                .create();

        final Pair<Entity.Column, ResultSetHandler<Object>> pkHandler =
                resultSetHandlerSelector.selectForPK(entity.getClass());

        if (pkHandler == null) {
            execute(sql);
        } else {
            execute(new ConnectionCallback<Void>() {
                @Override
                public Void doInConnection(Connection connection) throws SQLException {
                    Object pkValue = queryRunner.insert(
                            connection,
                            sql.getContent(),
                            pkHandler.getValue(),
                            sql.getParams()
                    );

                    pkHandler.getKey().setPropertyValue(entity, pkValue);
                    return null;
                }
            });
        }

        return entity;
    }

    @Override
    public <T> int[] insert(List<T> entities) {
        return insert(entities, null, false);
    }

    @Override
    public <T> int[] insert(List<T> entities, String activatedColumns, boolean ignoreNull) {
        if (CollectionUtil.isEmpty(entities)) {
            return EmptyValue.EMPTY_INT_ARRAY;
        }

        final Pair<Entity.Column, ResultSetHandler<Object>> idObjectHandler =
                resultSetHandlerSelector.selectForPK(entities.get(0).getClass());

        if (idObjectHandler == null) {
            return fastInsert(entities, activatedColumns, ignoreNull);
        } else {
            for (T entity : entities) {
                insert(entity, activatedColumns, ignoreNull);
            }

            int[] result = new int[entities.size()];
            Arrays.fill(result, 1);
            return result;
        }
    }

    @Override
    public <T> int[] fastInsert(List<T> entities) {
        return fastInsert(entities, null, false);
    }

    @Override
    public <T> int[] fastInsert(List<T> entities, final String activatedColumns, final boolean ignoreNull) {
        return execute(CollectionExUtil.collect(entities, new Function<T, Sql>() {
            @Override
            public Sql invoke(T entity) {
                return SqlBuilders.insert(entity)
                        .columns(activatedColumns)
                        .setInsertIgnoreNull(ignoreNull)
                        .create();
            }
        }));
    }

    @Override
    public int update(Object entity) {
        return update(entity, null, false);
    }

    @Override
    public int update(Object entity, String activatedColumns, boolean ignoreNull) {
        if (entity == null) {
            return 0;
        }

        return execute(SqlBuilders.update(entity)
                .columns(activatedColumns)
                .setUpdateIgnoreNull(ignoreNull)
                .create());
    }

    @Override
    public <T> int[] update(List<T> entities) {
        return update(entities, null, false);
    }

    @Override
    public <T> int[] update(List<T> entities, final String activatedColumns, final boolean ignoreNull) {
        return execute(CollectionExUtil.collect(entities, new Function<T, Sql>() {
            @Override
            public Sql invoke(T entity) {
                return SqlBuilders.update(entity)
                        .columns(activatedColumns)
                        .setUpdateIgnoreNull(ignoreNull)
                        .create();
            }
        }));
    }

    @Override
    public int delete(Object entity) {
        if (entity == null) {
            return 0;
        }

        return execute(SqlBuilders.delete(entity).create());
    }

    @Override
    public <T> int deleteWithPK(Class<T> resultClass, Object... ids) {
        if (ValueUtil.isEmpty(ids)) {
            return 0;
        }

        return execute(SqlBuilders.delete(resultClass).withPK(ids).create());
    }

    @Override
    public <T> int[] delete(List<T> entities) {
        return execute(CollectionExUtil.collect(entities, new Function<T, Sql>() {
            @Override
            public Sql invoke(T entity) {
                return SqlBuilders.delete(entity).create();
            }
        }));
    }

    @Override
    public int execute(final Sql sql) {
        if (sql == null) {
            return 0;
        }

        return execute(new ConnectionCallback<Integer>() {
            @Override
            public Integer doInConnection(Connection connection) throws SQLException {
                return queryRunner.update(connection, sql.getContent(), sql.getParams());
            }
        });
    }

    @Override
    public int[] execute(final List<Sql> sqlList) {
        if (CollectionUtil.isEmpty(sqlList)) {
            return EmptyValue.EMPTY_INT_ARRAY;
        }

        return execute(new ConnectionCallback<int[]>() {
            @Override
            public int[] doInConnection(Connection connection) throws SQLException {
                Object[][] objects = new Object[sqlList.size()][];
                for (int i = 0; i < sqlList.size(); i++) {
                    objects[i] = sqlList.get(i).getParams();
                }

                return queryRunner.batch(connection, sqlList.get(0).getContent(), objects);
            }
        });
    }

    @Override
    public <T> T execute(ConnectionCallback<T> connectionCallback) {
        Connection connection = null;

        try {
            connection = connectionHandler.getConnection(dataSource);
            return connectionCallback.doInConnection(connection);
        } catch (SQLException e) {
            throw ExceptionUtil.toRuntimeException(e);
        } finally {
            connectionHandler.releaseConnection(connection, dataSource);
        }
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    protected Sql createPagerSql(Sql sql, Pager pager) {
        if (pager == null) {
            return sql;
        }

        return new Sql()
                .setContent(dialect.createPageSelect(
                        sql.getContent(),
                        pager.getPageSize(),
                        pager.getOffset()))
                .setParams(sql.getParams());
    }
}