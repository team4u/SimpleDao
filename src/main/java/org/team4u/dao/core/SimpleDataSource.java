package org.team4u.dao.core;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class SimpleDataSource implements DataSource, Closeable {

    protected String username;
    protected String password;
    protected String driverClassName;
    protected String url;

    protected List<Connection> connections = new ArrayList<Connection>();

    public static DataSource createDataSource(Properties props) {
        SimpleDataSource sds = new SimpleDataSource();
        sds.setUrl(props.getProperty("url", props.getProperty("jdbcUrl")));
        sds.setPassword(props.getProperty("password"));
        sds.setUsername(props.getProperty("username"));
        return sds;
    }

    public Connection getConnection() throws SQLException {
        final Connection connection;
        if (username != null) {
            connection = DriverManager.getConnection(url, username, password);
        } else {
            connection = DriverManager.getConnection(url);
        }

        connections.add(connection);

        return (Connection) Proxy.newProxyInstance(connection.getClass().getClassLoader(), new Class[]{Connection.class}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("close") && method.getParameterTypes().length == 0)
                    connections.remove(connection);
                return method.invoke(connection, args);
            }
        });
    }

    @Override
    public synchronized void close() {
        List<Connection> connectionsToClose = new ArrayList<Connection>(connections);
        connections.clear();

        for (Connection connection : connectionsToClose) {
            try {
                connection.close();
            } catch (SQLException e) {
            }
        }
    }

    public void setDriverClassName(String driverClassName) throws ClassNotFoundException {
        this.driverClassName = driverClassName;
        Class.forName(driverClassName);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Connection getConnection(String username, String password)
            throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Logger getParentLogger() {
        throw new UnsupportedOperationException();
    }

    public long getActiveCount() {
        return connections.size();
    }
}