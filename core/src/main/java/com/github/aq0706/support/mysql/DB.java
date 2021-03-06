package com.github.aq0706.support.mysql;

import com.github.aq0706.lang.ReflectionUtil;
import com.github.aq0706.support.mysql.mapping.Column;
import com.github.aq0706.support.mysql.mapping.Key;
import com.github.aq0706.support.mysql.mapping.MyFieldMappingInfo;
import com.github.aq0706.support.mysql.mapping.Table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DB.model(Config.class).insert(config)
 * DB.model(Config.class).modify(["value" => "123"]).where("key = 1 and namespace = 1").execute()
 * DB.model(Config.class).delete().where("key = 1").execute()
 * DB.model(Config.class).select().where("key = 1").execute()
 * DB.model(Config.class).count("id").where("key = 1").query()
 *
 * @author lidq
 */
public class DB {

    private static Map<Class, MyFieldMappingInfo> tableInfoMap = new ConcurrentHashMap<>();

    public static void initTableInfo(String packageName) throws ClassNotFoundException {
        Collection<Class> classes = ReflectionUtil.findClassByPackage(packageName);
        for (Class beanClass : classes) {
            initTableInfo(beanClass);
        }
    }

    public static void initTableInfo(Class beanClass) {
        Table table = (Table) beanClass.getAnnotation(Table.class);
        if (table != null) {
            MyFieldMappingInfo tableInfo = new MyFieldMappingInfo();
            tableInfo.tableName = table.name();
            tableInfo.schema = table.schema();
            Field[] fields = beanClass.getDeclaredFields();
            tableInfo.columns = new ArrayList<>(fields.length);

            for (Field field : fields) {
                Column column = field.getAnnotation(Column.class);
                if (column != null) {
                    boolean isAutoIncrement = false;
                    Key key = field.getAnnotation(Key.class);
                    if (key != null) {
                        isAutoIncrement = key.isAutoIncrement();
                    }
                    tableInfo.columns.add(new MyFieldMappingInfo.Column(field.getName(), column.name(), key != null, isAutoIncrement));
                }
            }
            tableInfoMap.putIfAbsent(beanClass, tableInfo);
        }
    }

    public static <T> DBExecutor<T> model(Class<T> clazz) {
        MyFieldMappingInfo tableInfo = tableInfoMap.get(clazz);
        if (tableInfo == null) {
            initTableInfo(clazz);
        }
        tableInfo = tableInfoMap.get(clazz);
        if (tableInfo == null) {
            throw new IllegalArgumentException("Cannot find tableInfo for class:" + clazz.getName());
        }

        return new DBExecutor<>(tableInfo, clazz);
    }
}
