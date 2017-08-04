package org.team4u.dao.transaction;

import com.xiaoleilu.hutool.log.Log;
import com.xiaoleilu.hutool.log.LogFactory;
import org.team4u.kit.core.error.ExceptionUtil;

import java.sql.Connection;

/**
 * @author Jay Wu
 */
public class Transactions {

    public static final int DEFAULT_LEVEL = Connection.TRANSACTION_READ_COMMITTED;
    private static final Log log = LogFactory.get();
    private static ThreadLocal<Transaction> transactionThreadLocal = new ThreadLocal<Transaction>();

    public static Transaction get() {
        return transactionThreadLocal.get();
    }

    public static void begin(int level) {
        Transaction transaction = get();

        if (transaction == null) {
            transaction = new Transaction(level);
            log.trace("Initializing Transaction");
            transactionThreadLocal.set(transaction);
        }

        transaction.begin();
    }

    public static void commit() {
        Transaction transaction = get();

        if (transaction == null) {
            return;
        }

        transaction.commit();
    }

    public static void close() {
        Transaction transaction = get();

        if (transaction == null) {
            return;
        }

        try {
            transaction.close();
        } finally {
            if (transaction.shouldClose()) {
                log.trace("Clearing Transaction");
                transactionThreadLocal.set(null);
            }
        }
    }

    public static void rollback() {
        Transaction transaction = get();

        if (transaction == null) {
            return;
        }

        transaction.rollback();
    }

    public static void execute(Runnable... runnableList) {
        execute(DEFAULT_LEVEL, runnableList);
    }

    public static void execute(int level, Runnable... runnableList) {
        try {
            begin(level);

            for (Runnable runnable : runnableList) {
                runnable.run();
            }

            commit();
        } catch (Exception e) {
            rollback();
            throw ExceptionUtil.toRuntimeException(e);
        } finally {
            close();
        }
    }
}