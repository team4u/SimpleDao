package org.team4u.dao;

import org.team4u.kit.core.error.ServiceException;

/**
 * @author Jay Wu
 */
public class SqlNotFoundException extends ServiceException {
    public SqlNotFoundException(String message) {
        super(message);
    }

    public SqlNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqlNotFoundException(Throwable cause) {
        super(cause);
    }
}