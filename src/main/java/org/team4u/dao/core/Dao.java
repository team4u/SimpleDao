package org.team4u.dao.core;

import org.apache.commons.dbutils.ResultSetHandler;
import org.team4u.kit.core.action.Callback;
import org.team4u.kit.core.lang.Pager;
import org.team4u.sql.builder.Sql;
import org.team4u.sql.builder.entity.builder.EntitySqlBuilder;

import java.util.List;

/**
 * @author Jay Wu
 */
public interface Dao {

    /**
     * 根据主键查询对象
     *
     * @return 对象
     */
    <T> T queryWithPkForObject(Class<T> resultClass, Object... ids);

    /**
     * 查询单个对象
     *
     * @return 对象
     */
    <T> T queryForObject(Class<T> resultClass, Sql sql);

    /**
     * 查询单个对象
     *
     * @return 对象
     */
    <T> T queryForObject(EntitySqlBuilder<T> sqlBuilder);

    /**
     * 查询一组对象
     *
     * @return 对象集合
     */
    <T> List<T> queryForList(Class<T> resultClass, Sql sql);

    /**
     * 查询一组对象
     *
     * @return 对象集合
     */
    <T> List<T> queryForList(EntitySqlBuilder<T> sqlBuilder);

    /**
     * 根据分页查询一组对象
     *
     * @return 对象集合
     */
    <T> List<T> queryForList(Class<T> resultClass, Sql sql, Pager pager);

    /**
     * 根据分页查询一组对象
     *
     * @return 对象集合
     */
    <T> List<T> queryForList(EntitySqlBuilder<T> sqlBuilder, Pager pager);

    /**
     * 查询数据,返回结果由handler进行处理
     *
     * @return 结果对象
     */
    <T> T query(ResultSetHandler<T> handler, Sql sql);

    /**
     * 查询记录数量
     *
     * @return 记录数
     */
    int count(Sql sql);

    /**
     * 对一组对象进行迭代，适用于大量数据的集合
     */
    <T> void each(Class<T> resultClass, Sql sql, Callback<T> callback);

    /**
     * 对一组对象进行迭代，适用于大量数据的集合
     */
    <T> void each(EntitySqlBuilder<T> sqlBuilder, Callback<T> callback);

    /**
     * 对一组对象按指定分页进行迭代，适用于大量数据的集合
     */
    <T> void each(Class<T> resultClass, Sql sql, Pager pager, Callback<T> callback);

    /**
     * 对一组对象按指定分页进行迭代，适用于大量数据的集合
     */
    <T> void each(EntitySqlBuilder<T> sqlBuilder, Pager pager, Callback<T> callback);

    /**
     * 将一个对象插入到一个数据源。
     * <p>
     * 如果你的字段声明了 '@Id(auto=true)'，则填充插入后最新的 ID 值
     *
     * @return 当前对象
     */
    <T> T insert(T entity);

    /**
     * 将一个对象集合插入到一个数据源。
     * <p>
     * 如果你的字段声明了 '@Id(auto=true)'，则填充插入后最新的 ID 值;若auto=false,则内部采用fastInsert处理
     *
     * @return 已插入记录数
     */
    <T> int[] insert(List<T> entities);

    /**
     * 快速插入一个对象
     * <p>
     * '@Id(auto=true)'将不起作用,内部统一采用 batch 的方法插入
     *
     * @return 影响的行数
     */
    <T> int[] fastInsert(List<T> entities);

    /**
     * 更新对象
     */
    int update(Object entity);

    /**
     * 批量更新对象
     */
    <T> int[] update(List<T> entities);

    /**
     * 删除对象
     *
     * @return 影响的行数
     */
    int delete(Object entity);

    /**
     * 根据主键删除对象
     *
     * @return 影响的行数
     */
    <T> int deleteWithPK(Class<T> resultClass, Object... ids);

    /**
     * 批量删除对象
     *
     * @return 影响的行数
     */
    <T> int[] delete(List<T> entities);

    /**
     * 执行SQL语句
     *
     * @return 影响的行数
     */
    int execute(Sql sql);

    /**
     * 批量执行SQL语句
     *
     * @return 影响的行数
     */
    int[] execute(List<Sql> sql);

    /**
     * 通过ConnectionCallback得到一个 Connection 接口实例
     * <p>
     * 请注意，不要手工关闭这个连接，方法执行完毕后会自动关闭连接。
     * <p>
     * 如果你从当前连接对象中创建了ResultSet对象或者 Statement对象，请自行关闭。
     *
     * @return ConnectionCallback返回结果
     */
    <T> T execute(ConnectionCallback<T> connectionCallback);
}