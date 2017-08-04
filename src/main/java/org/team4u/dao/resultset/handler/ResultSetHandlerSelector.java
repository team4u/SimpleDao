package org.team4u.dao.resultset.handler;

import org.apache.commons.dbutils.ResultSetHandler;
import org.team4u.kit.core.action.Callback;
import org.team4u.kit.core.lang.Pair;
import org.team4u.sql.builder.entity.Entity;

import java.util.List;

/**
 * @author Jay Wu
 */
public interface ResultSetHandlerSelector {

    <T> Pair<Entity.Column, ResultSetHandler<Object>> selectForPK(Class<T> clazz);

    <T> ResultSetHandler<T> selectForObject(Class<T> clazz);

    <T> ResultSetHandler<List<T>> selectForList(Class<T> clazz);

    <T> ResultSetHandler<T> selectForCallback(Class<T> clazz, Callback<T> callback);
}