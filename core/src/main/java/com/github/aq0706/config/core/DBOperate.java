package com.github.aq0706.config.core;

import com.github.aq0706.config.pool.sql.SQLConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lidq
 */
public class DBOperate {

    private static SQLConnectionPool pool = SQLConnectionPool.getDefault();
    private static Map<Class, String> beanClassCache = new HashMap<>();

    private Action action;
    private Class beanClass;
    private Map<String, ColumnsInfo> beanColumnsInfo;

    public DBOperate Action(Action action) {
        this.action = action;
        return this;
    }

    public DBOperate Columns(Class columns) {
        this.beanClass = columns;
        return this;
    }

    public void save(Object data) throws SQLException {
        checkState();

        Connection connection = pool.getConnection();
    }

    private void checkState() {
        if (action == null) {
            throw new IllegalStateException("Action can not be null.");
        }
        if (beanClass == null) {
            throw new IllegalStateException("BeanClass can not be null.");
        }
        if (beanColumnsInfo == null) {
            throw new IllegalStateException("BeanColumnsInfo can not be null.");
        }
    }

    private static class ColumnsInfo {
        String name;
        boolean isAutoIncrement;
    }

    private enum Action {
        INSERT, UPDATE, DELETE, SELECT
    }
}
