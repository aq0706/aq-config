package com.github.aq0706.support.mysql;

import com.github.aq0706.support.mysql.mapping.MyFieldMappingInfo;
import com.github.aq0706.support.mysql.pool.SQLConnectionPool;
import javafx.util.Pair;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lidq
 */
public class DBExecutor<T> {

    private static SQLConnectionPool pool = SQLConnectionPool.getDefault();

    private final MyFieldMappingInfo tableInfo;
    private final Class<T> beanClass;

    private byte action = 0;
    private static final byte INSERT_ACTION = 1;
    private static final byte MODIFY_ACTION = 2;
    private static final byte SELECT_ACTION = 3;
    private static final byte COUNT_ACTION = 4;
    private static final byte DELETE_ACTION = 5;

    private Collection<Pair<String/* filed */, Object/* value */>> modifyList;
    private String whereCondition;

    public DBExecutor(MyFieldMappingInfo tableInfo, Class<T> beanClass) {
        this.tableInfo = tableInfo;
        this.beanClass = beanClass;
    }

    public long insert(T bean) throws SQLException {
        if (action != 0) {
            throw new IllegalStateException("Action is not 0");
        }
        action = INSERT_ACTION;

        Map<MyFieldMappingInfo.Column, Object> insertColumnsDataMap = new LinkedHashMap<>();
        List<String> insertColumns = new ArrayList<>(tableInfo.columns.size());
        List<String> preValues = new ArrayList<>(tableInfo.columns.size());
        try {
            for (int i = 0; i < tableInfo.columns.size(); i++) {
                MyFieldMappingInfo.Column column = tableInfo.columns.get(i);
                if (!column.isAutoIncrement) {
                    Field field = beanClass.getDeclaredField(column.fieldName);
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    Object value = field.get(bean);
                    if (value != null) {
                        insertColumnsDataMap.put(column, value);
                        insertColumns.add(column.mySQLFieldName);
                        preValues.add("?");
                    }
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new SQLException(e.getMessage());
        }

        String sqlFormat = "INSERT INTO " + tableInfo.schema + "." + tableInfo.tableName + " (%s) VALUES (%s);";
        String sql = String.format(sqlFormat, String.join(",", insertColumns), String.join(",", preValues));

        return execute(sql, insertColumnsDataMap.values());
    }

    @SafeVarargs
    public final DBExecutor<T> modify(Pair<String/* filed */, Object/* value */>... modifyList) {
        if (action != 0) {
            throw new IllegalStateException("Action is not 0");
        }
        action = MODIFY_ACTION;

        this.modifyList = Arrays.asList(modifyList);

        return this;
    }

    public final DBExecutor<T> select() {
        if (action != 0) {
            throw new IllegalStateException("Action must not 0");
        }
        action = SELECT_ACTION;

        return this;
    }

    public final DBExecutor<T> count() {
        if (action != 0) {
            throw new IllegalStateException("Action must not 0");
        }
        action = COUNT_ACTION;

        return this;
    }

    public final DBExecutor<T> where() {
        return where("1 = 1");
    }

    public final DBExecutor<T> where(String whereCondition) {
        if (action == 0) {
            throw new IllegalStateException("Action must not 0");
        }

        this.whereCondition = whereCondition;

        return this;
    }

    public final long execute() throws SQLException {
        if (action == 0) {
            throw new IllegalStateException("Action must not 0");
        }
        if (action == MODIFY_ACTION && modifyList.isEmpty()) {
            throw new IllegalStateException("No properties to modify");
        }
        if (whereCondition.isEmpty()) {
            throw new IllegalStateException("Please call where first");
        }

        if (action == MODIFY_ACTION) {
            String sqlFormat = "UPDATE " + tableInfo.schema + "." + tableInfo.tableName + " SET %s WHERE %s;";
            List<String> setSqlList = modifyList.stream().map(modify -> (modify.getKey() + "=?")).collect(Collectors.toList());
            String setSql = String.join(",", setSqlList);
            String sql = String.format(sqlFormat, setSql, whereCondition);
            List<Object> setValue = modifyList.stream().map(Pair::getValue).collect(Collectors.toList());
            return execute(sql, setValue);
        } else if (action == DELETE_ACTION) {
            String sqlFormat = "DELETE FROM " + tableInfo.schema + "." + tableInfo.tableName + " WHERE %s;";
            String sql = String.format(sqlFormat, whereCondition);
            return execute(sql, Collections.emptyList());
        } else if (action == COUNT_ACTION) {
            String sqlFormat = "SELECT count(*) FROM " + tableInfo.schema + "." + tableInfo.tableName + " WHERE %s;";
            String sql = String.format(sqlFormat, whereCondition);
            return count(sql, Collections.emptyList());
        }

        throw new IllegalStateException("Unknown action: " + action);
    }

    public final Collection<T> query() throws SQLException {
        if (action != SELECT_ACTION) {
            throw new IllegalStateException("Action must be " + SELECT_ACTION);
        }
        if (whereCondition.isEmpty()) {
            throw new IllegalStateException("Please call where first");
        }

        String sqlFormat = "SELECT %s FROM " + tableInfo.schema + "." + tableInfo.tableName + " WHERE %s;";
        String properties = tableInfo.columns.stream().map(column -> column.mySQLFieldName).collect(Collectors.joining(","));
        String sql = String.format(sqlFormat, properties, whereCondition);
        return query(sql, Collections.emptyList());
    }

    private long execute(String sql, Collection<Object> dataList) throws SQLException {
        try (Connection connection = pool.getConnection();
             PreparedStatement stmt = createPreparedStatement(connection, sql, dataList)) {
            return stmt.executeUpdate();
        }
    }

    private long count(String sql, Collection<Object> dataList) throws SQLException {
        try (Connection connection = pool.getConnection();
             PreparedStatement stmt = createPreparedStatement(connection, sql, dataList)) {
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            return 0L;
        }
    }

    private List<T> query(String sql, Collection<Object> dataList) throws SQLException {
        List<T> results = new ArrayList<>();
        try (Connection connection = pool.getConnection();
             PreparedStatement stmt = createPreparedStatement(connection, sql, dataList)) {
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                T result = beanClass.newInstance();
                for (int i = 0; i < tableInfo.columns.size(); i++) {
                    MyFieldMappingInfo.Column column = tableInfo.columns.get(i);
                    Field field = beanClass.getDeclaredField(column.fieldName);
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    Object value = resultSet.getObject(i + 1, field.getType());
                    field.set(result, value);
                }
                results.add(result);
            }
        } catch (IllegalAccessException | InstantiationException | NoSuchFieldException e) {
            e.printStackTrace();
            throw new SQLException(e);
        }

        return results;
    }

    private PreparedStatement createPreparedStatement(Connection connection, String sql, Collection<Object> dataList) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        int index = 1;
        for (Object data : dataList) {
            if (data.getClass() == Boolean.class || data.getClass() == boolean.class) {
                stmt.setBoolean(index, (Boolean) data);
            } else if (data.getClass() == Integer.class || data.getClass() == int.class) {
                stmt.setInt(index, (Integer) data);
            } else if (data.getClass() == Long.class || data.getClass() == long.class) {
                stmt.setLong(index, (Long) data);
            } else if (data.getClass() == Float.class || data.getClass() == float.class) {
                stmt.setFloat(index, (Float) data);
            } else if (data.getClass() == Double.class || data.getClass() == double.class) {
                stmt.setDouble(index, (Double) data);
            } else if (data.getClass() == BigDecimal.class) {
                stmt.setBigDecimal(index, (BigDecimal) data);
            } else if (data.getClass() == String.class) {
                stmt.setString(index, (String) data);
            } else if (data.getClass() == Date.class) {
                stmt.setDate(index, (Date) data);
            } else if (data.getClass() == Time.class) {
                stmt.setTime(index, (Time) data);
            } else if (data.getClass() == Timestamp.class) {
                stmt.setTimestamp(index, (Timestamp) data);
            }
            index++;
        }
        return stmt;
    }
}
