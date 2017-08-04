package org.team4u.dao.transaction;

import com.xiaoleilu.hutool.log.Log;
import com.xiaoleilu.hutool.log.LogFactory;
import org.apache.commons.dbutils.DbUtils;
import org.team4u.kit.core.action.Callback;
import org.team4u.kit.core.error.ComboException;
import org.team4u.kit.core.error.ExceptionUtil;

import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jay Wu
 */
public class Transaction implements Closeable {

    private static final Log log = LogFactory.get();
    protected final AtomicInteger retain;
    protected final Stack<Integer> retainBeginStack;
    protected final Map<DataSource, Resource> resources;
    protected int level;

    public Transaction(int level) {
        this.level = level;

        retain = new AtomicInteger(0);
        retainBeginStack = new Stack<Integer>();
        resources = new HashMap<DataSource, Resource>();
    }

    public void begin() {
        log.trace("Starting Transaction");
        retainBeginStack.add(retain.getAndIncrement());
    }

    public void commit() {
        retain.decrementAndGet();

        execute(new Callback<Resource>() {
            @Override
            public void invoke(Resource resource) {
                try {
                    if (log.isTraceEnabled()) {
                        log.trace("Committing Transaction Resource(connection={})", resource.getConnection());
                    }
                    resource.commit();
                } catch (SQLException e) {
                    throw ExceptionUtil.toRuntimeException(e);
                }
            }
        });
    }

    public void rollback() {
        retain.set(retainBeginStack.pop());

        execute(new Callback<Resource>() {
            @Override
            public void invoke(Resource resource) {
                try {
                    log.trace("Rollbacking Transaction Resource(connection={})", resource.getConnection());
                    resource.rollback();
                } catch (SQLException e) {
                    throw ExceptionUtil.toRuntimeException(e);
                }
            }
        });
    }

    @Override
    public void close() {
        execute(new Callback<Resource>() {

            @Override
            public void invoke(Resource resource) {
                try {
                    log.trace("Closing Transaction Resource(connection={})", resource.getConnection());
                    resource.close();
                } catch (SQLException e) {
                    // Ignore error
                }
            }
        });
    }

    public Connection getConnection(DataSource dataSource) throws SQLException {
        Resource resource = resources.get(dataSource);

        if (resource == null || resource.getConnection() == null) {
            log.debug("Creating Transaction Resource from DataSource");
            resource = new Resource(dataSource);
            resources.put(dataSource, resource);
        } else {
            log.debug("Fetching resumed JDBC Connection from Transaction Resource");
        }

        return resource.getConnection();
    }

    public boolean shouldClose() {
        return retain.get() == 0;
    }

    protected void execute(Callback<Resource> callback) {
        if (retain.get() == 0) {
            ComboException ce = new ComboException();

            for (Resource resource : resources.values()) {
                try {
                    callback.invoke(resource);
                } catch (Exception e) {
                    ce.add(e);
                }
            }

            // 如果有一个数据源提交时发生异常，抛出
            if (null != ce.getCause()) {
                throw ce;
            }
        }
    }

    protected class Resource {

        private Connection connection;
        private int oldLevel;
        private boolean oldAutoCommit;

        public Resource(DataSource dataSource) throws SQLException {
            connection = dataSource.getConnection();
            oldAutoCommit = connection.getAutoCommit();
            oldLevel = connection.getTransactionIsolation();

            connection.setAutoCommit(false);
            connection.setTransactionIsolation(level);
        }

        public void commit() throws SQLException {
            connection.commit();

            connection.setAutoCommit(oldAutoCommit);
            connection.setTransactionIsolation(oldLevel);
        }

        public void close() throws SQLException {
            DbUtils.close(connection);
        }

        public void rollback() throws SQLException {
            connection.rollback();
        }

        public Connection getConnection() {
            return connection;
        }
    }
}